package ru.urfu.webapplication.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.urfu.webapplication.entity.PasswordResetCode;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.PasswordResetCodeRepository;
import ru.urfu.webapplication.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ApiKeyService apiKeyService;
    private final PasswordResetCodeRepository passwordResetCodeRepository;

    //Генерация 6-значного кода для восстановления пароля
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public Map<String, String> registerUser(String email, String password, String confirmPassword) {

        if (!password.equals(confirmPassword)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Пароли не совпадают");
            response.put("message", "Введённые пароли отличаются");
            return response;
        }

        if (userRepository.findByEmail(email).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Email уже зарегистрирован");
            response.put("message", "Этот email уже существует");
            return response;
        }

        SubscriptionLevel level = SubscriptionLevel.FREE;

        String apiKey = generateApiKey(email, level);
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setApiKey(apiKey);
        user.setSubscriptionLevel(level);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
        //Отправка письма
        emailService.sendApiKeyEmail(email, apiKey);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("apiKey", apiKey);
        response.put("subscriptionLevel", level.name());
        response.put("message", "Регистрация прошла успешно! Сохраните ваш API ключ!");
        log.info("Новый пользователь успешно зарегистрирован: {} с FREE планом", email);
        return response;
    }

    private String generateApiKey(String email, SubscriptionLevel level) {
        String prefix = switch (level) {
            case FREE -> "free";
            case BASIC -> "basic";
            case PREMIUM -> "premium";
        };
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return prefix + "-" + uniqueId + "-" + Math.abs(email.hashCode());
    }

    //Понижение подписки при истечении срока
    @Transactional
    public void subscriptionReduction(String apiKey) {
        User user = userRepository.findByApiKey(apiKey).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (user.getSubscriptionExpiresAt() == null) {
            return;
        }
        if (user.getSubscriptionExpiresAt().isBefore(LocalDateTime.now())) {
            SubscriptionLevel oldLevel = user.getSubscriptionLevel();
            SubscriptionLevel newLevel = SubscriptionLevel.FREE;
            //обновление ключа
            String newApiKey = apiKeyService.generateApiKey(user.getEmail(), newLevel.name());
            apiKeyService.deactivateKey(apiKey);
            //обновление пользователя
            user.setApiKey(newApiKey);
            user.setSubscriptionLevel(newLevel);
            user.setSubscriptionExpiresAt(null);
            user.setIsActive(true);
            userRepository.save(user);

            log.info("Подписка пользователя {} истекла. Понижена с {} до FREE. Новый ключ: {}", user.getEmail(), oldLevel, newApiKey);
            emailService.sendSubscriptionExpiredEmail(user.getEmail(), oldLevel.name(), newApiKey);
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + email));
    }

    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
        log.info("Пользователь {} обновлён", user.getEmail());
    }

    //Запрос на восстановление пароля
    @Transactional
    public Map<String, String> forgotPassword(String email) {
        log.info("Запрос на сброс и восстановление пароля для пользователя {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с таким email не найден"));
        //Удаляем старые коды
        passwordResetCodeRepository.deleteByEmail(email);
        //Генерируем и сохраняем новый код
        String resetCode = generateCode();
        PasswordResetCode resetCodeEntity = new PasswordResetCode();
        resetCodeEntity.setEmail(email);
        resetCodeEntity.setCode(resetCode);
        resetCodeEntity.setCreatedAt(LocalDateTime.now());
        resetCodeEntity.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        passwordResetCodeRepository.save(resetCodeEntity);
        log.info("Сгенерирован код сброса пароля для пользователя {}", email);
        emailService.sendPasswordResetCode(email, resetCode);
        return Map.of(
                "message", "Код для сброса пароля отправлен на вашу почту.",
                "email", email
        );
    }

    //Сброс пароля с кодом подтверждения
    @Transactional
    public Map<String, String> resetPassword(String email, String code, String newPassword) {
        log.info("Попытка сброса пароля для пользователя {}", email);
        //Проверяем код
        boolean isValid = passwordResetCodeRepository
                .findByEmailAndCodeAndExpiresAtAfter(email, code, LocalDateTime.now())
                .isPresent();
        if (!isValid) {
            throw new RuntimeException("Неверный или просроченный код подтверждения");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        //Меняем пароль
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        //Удаляем использованный код
        passwordResetCodeRepository.deleteByEmail(email);
        log.info("Пароль для пользователя {} был сброшен и восстановлен", email);
        emailService.sendPasswordChangedEmail(email);
        return Map.of(
                "message", "Пароль успешно изменен. Используйте новый пароль для входа",
                "success", "true"
        );
    }

    //Смена пароля
    @Transactional
    public Map<String, String> changePassword(String email, String oldPassword, String newPassword) {
        log.info("Попытка смены пароля для пользователя {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Неверный текущий пароль");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Пароль для пользователя {} был изменен", email);
        emailService.sendPasswordChangedEmail(email);
        return Map.of(
                "message", "Пароль успешно изменён",
                "success", "true"
        );
    }
}