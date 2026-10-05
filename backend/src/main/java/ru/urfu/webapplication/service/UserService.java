package ru.urfu.webapplication.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.repository.UserRepository;
import ru.urfu.webapplication.repository.WeatherRequestRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ApiKeyService apiKeyService;
    private final WeatherRequestRepository requestRepository;

    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
        log.info("[auth] Пользователь {} обновлён", user.getEmail());
    }

    //Получение профиля
    public Map<String, Object> getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        int maxRequests = apiKeyService.getMaxRequests(user.getSubscriptionLevel());
        long usedRequests = 0;
        int remainingRequests = maxRequests;
        if (maxRequests != Integer.MAX_VALUE) {
            LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
            usedRequests = requestRepository.countRequestsByUserInLast24Hours(user, twentyFourHoursAgo);
            remainingRequests = (int) Math.max(0, maxRequests - usedRequests);
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("email", user.getEmail());
        profile.put("apiKey", user.getApiKey());
        profile.put("subscriptionLevel", user.getSubscriptionLevel());
        profile.put("subscriptionExpiresAt", user.getSubscriptionExpiresAt());
        profile.put("autoRenewal", user.getAutoRenewal());
        profile.put("createdAt", user.getCreatedAt());

        Map<String, Object> limits = new HashMap<>();
        limits.put("dailyLimit", maxRequests == Integer.MAX_VALUE ? "Неограничено" : maxRequests);
        limits.put("usedToday", usedRequests);
        limits.put("remainingToday", remainingRequests == Integer.MAX_VALUE ? "Неограничено" : remainingRequests);
        profile.put("limits", limits);

        log.info("[auth] Пользователь {} получил информацию о своем профиле", user.getEmail());
        return profile;
    }

    //Изменение автопродления подписки
    @Transactional
    public Map<String, Object> setAutoRenewal(String email, boolean enabled) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setAutoRenewal(enabled);
        updateUser(user);

        log.info("[auth] Пользователь {} изменил автопродление подписки на {}", email, enabled);
        return Map.of(
                "success", true,
                "autoRenewal", enabled,
                "message", String.format("Автопродление %s", enabled ? "включено" : "отключено")
        );
    }

    //Удаление аккаунта
    public Map<String, String> deleteAccount(String email, String password, HttpServletResponse response) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Неверный пароль");
            error.put("message", "Пароль не совпадает");
            return error;
        }

        apiKeyService.deactivateKey(user.getApiKey());
        userRepository.delete(user);
        emailService.sendAccountDeletedEmail(email);

        Cookie cookie = new Cookie("apiKey", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        log.info("[auth] Пользователь {} удалил аккаунт", email);
        return Map.of(
                "success", "true",
                "message", "Аккаунт успешно удалён"
        );
    }

    //Смена пароля
    @Transactional
    public Map<String, String> changePassword(String email, String oldPassword, String newPassword) {
        log.info("[auth] Попытка смены пароля для пользователя {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Неверный текущий пароль");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("[auth] Пароль для пользователя {} был изменен", email);
        emailService.sendPasswordChangedEmail(email);
        return Map.of(
                "message", "Пароль успешно изменён",
                "success", "true"
        );
    }
}