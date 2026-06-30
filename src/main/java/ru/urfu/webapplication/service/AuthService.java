package ru.urfu.webapplication.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.webapplication.entity.PasswordResetCode;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.PasswordResetCodeRepository;
import ru.urfu.webapplication.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final EmailService emailService;
    private final ApiKeyService apiKeyService;
    @Value("${password.reset.expire-minutes}")
    private int resetCodeExpireMinutes;

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

        String apiKey = apiKeyService.generateApiKey(email, level);
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setApiKey(apiKey);
        user.setSubscriptionLevel(level);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
        emailService.sendApiKeyEmail(email, apiKey);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("apiKey", apiKey);
        response.put("subscriptionLevel", level.name());
        response.put("message", "Регистрация прошла успешно!");
        log.info("Новый пользователь успешно зарегистрирован: {} с FREE планом", email);
        return response;
    }

    public Map<String, Object> login(String email, String password, HttpServletResponse response) {
        log.info("Попытка входа пользователя {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден: {}", email);
                    return new RuntimeException("Неверный email или пароль");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Неверный email или пароль");
        }

        log.info("Успешный вход пользователя {}", email);
        String apiKey = user.getApiKey();

        Cookie cookie = new Cookie("apiKey", apiKey);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30);
        response.addCookie(cookie);

        return Map.of(
                "success", true,
                "email", user.getEmail(),
                "subscriptionLevel", user.getSubscriptionLevel(),
                "apiKey", apiKey
        );
    }

    public Map<String, String> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("apiKey", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return Map.of("message", "Вы вышли из системы");
    }

    public Map<String, Object> checkAuth(String apiKey) {
        if (apiKey == null) {
            return Map.of("authenticated", false, "message", "Не авторизован");
        }

        User user = userRepository.findByApiKey(apiKey).orElse(null);
        if (user == null || !user.getIsActive()) {
            return Map.of("authenticated", false, "message", "Неверный или неактивный API ключ");
        }

        return Map.of(
                "authenticated", true,
                "email", user.getEmail(),
                "subscriptionLevel", user.getSubscriptionLevel(),
                "apiKey", apiKey
        );
    }

    @Transactional
    public Map<String, String> forgotPassword(String email) {
        log.info("Запрос на сброс и восстановление пароля для пользователя {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с таким email не найден"));

        passwordResetCodeRepository.deleteByEmail(email);
        String resetCode = generateCode();

        PasswordResetCode resetCodeEntity = new PasswordResetCode();
        resetCodeEntity.setEmail(email);
        resetCodeEntity.setCode(resetCode);
        resetCodeEntity.setCreatedAt(LocalDateTime.now());
        resetCodeEntity.setExpiresAt(LocalDateTime.now().plusMinutes(resetCodeExpireMinutes));
        passwordResetCodeRepository.save(resetCodeEntity);

        log.info("Сгенерирован код сброса пароля для пользователя {}", email);
        emailService.sendPasswordResetCode(email, resetCode);

        return Map.of(
                "message", "Код для сброса пароля отправлен на вашу почту.",
                "email", email
        );
    }

    @Transactional
    public Map<String, String> resetPassword(String email, String code, String newPassword) {
        log.info("Попытка сброса пароля для пользователя {}", email);

        boolean isValid = passwordResetCodeRepository
                .findByEmailAndCodeAndExpiresAtAfter(email, code, LocalDateTime.now())
                .isPresent();
        if (!isValid) {
            throw new RuntimeException("Неверный или просроченный код подтверждения");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetCodeRepository.deleteByEmail(email);

        log.info("Пароль для пользователя {} был сброшен и восстановлен", email);
        emailService.sendPasswordChangedEmail(email);

        return Map.of(
                "message", "Пароль успешно изменен. Используйте новый пароль для входа",
                "success", "true"
        );
    }
}