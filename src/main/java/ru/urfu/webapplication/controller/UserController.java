package ru.urfu.webapplication.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.repository.UserRepository;
import ru.urfu.webapplication.repository.WeatherRequestRepository;
import ru.urfu.webapplication.security.WeatherUserDetails;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.service.ApiKeyService;
import ru.urfu.webapplication.service.EmailService;
import ru.urfu.webapplication.service.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final WeatherRequestRepository requestRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ApiKeyService apiKeyService;
    private final EmailService emailService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof WeatherUserDetails userDetails) {
            String email = userDetails.getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        }
        throw new RuntimeException("Пользователь не авторизован");
    }

    private int getMaxRequests(SubscriptionLevel level) {
        return switch (level) {
            case FREE -> 10;
            case BASIC -> 100;
            case PREMIUM -> Integer.MAX_VALUE;
        };
    }

    //Получение профиля пользователя
    //curl -X GET "http://localhost:8080/user/profile" -b cookies.txt
    @GetMapping("/profile")
    public Map<String, Object> getProfile() {
        User user = getCurrentUser();
        //Расчет оставшихся запросов за сегодня
        int maxRequests = getMaxRequests(user.getSubscriptionLevel());
        long usedRequests = 0;
        int remainingRequests = maxRequests;
        if (maxRequests != Integer.MAX_VALUE) {
            LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
            usedRequests = requestRepository.countRequestsByKeyInLast24Hours(user.getApiKey(), twentyFourHoursAgo);
            remainingRequests = (int) Math.max(0, maxRequests - usedRequests);
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("email", user.getEmail());
        profile.put("apiKey", user.getApiKey());
        profile.put("subscriptionLevel", user.getSubscriptionLevel());
        profile.put("subscriptionExpiresAt", user.getSubscriptionExpiresAt());
        profile.put("autoRenewal", user.getAutoRenewal());
        profile.put("createdAt", user.getCreatedAt());

        //Лимиты запросов
        Map<String, Object> limits = new HashMap<>();
        limits.put("dailyLimit", maxRequests == Integer.MAX_VALUE ? "Неограничено" : maxRequests);
        limits.put("usedToday", usedRequests);
        limits.put("remainingToday", remainingRequests == Integer.MAX_VALUE ? "Неограничено" : remainingRequests);
        profile.put("limits", limits);

        log.info("Пользователь {} получил информацию о своем профиле", user.getEmail());
        return profile;
    }

    // Включить/отключить автопродление подписки
    //http://localhost:8080/user/auto-renewal?enabled=true
    //http://localhost:8080/user/auto-renewal?enabled=false
    @GetMapping("/auto-renewal")
    public Map<String, Object> setAutoRenewal(
            @RequestParam(required = false) String apiKey,
            @RequestParam boolean enabled) {

        User user = getCurrentUser();
        user.setAutoRenewal(enabled);
        userService.updateUser(user);

        log.info("Пользователь {} изменил автопродление подписки на {}", user.getEmail(),  enabled);
        return Map.of(
                "success", true,
                "autoRenewal", enabled,
                "message", String.format("Автопродление %s", enabled ? "включено" : "отключено")
        );
    }

    //Удаление аккаунта с подтверждением пароля
    //curl -X DELETE "http://localhost:8080/user/account?password=123456" -b cookies.txt
    @DeleteMapping("/account")
    public Map<String, String> deleteAccount(@RequestParam @NotBlank String password, HttpServletResponse response) {
        User user = getCurrentUser();
        String userEmail = user.getEmail();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Неверный пароль");
            error.put("message", "Пароль не совпадает");
            return error;
        }

        apiKeyService.deactivateKey(user.getApiKey());
        userRepository.delete(user);
        emailService.sendAccountDeletedEmail(userEmail);
        //Очищаем куки
        Cookie cookie = new Cookie("apiKey", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        //Очищаем контекст безопасности
        SecurityContextHolder.clearContext();
        log.info("Пользователь {} удалил аккаунт", user.getEmail());
        return Map.of(
                "success", "true",
                "message", "Аккаунт успешно удалён"
        );
    }

    //Смена пароля пользователя
    //POST http://localhost:8080/user/change-password?oldPassword=123456&newPassword=123456
    @PostMapping("/change-password")
    public Map<String, String> changePassword(
            @RequestParam @NotBlank String oldPassword,
            @RequestParam @NotBlank @Size(min = 6, message = "Пароль должен содержать минимум 6 символов") String newPassword) {
        User user = getCurrentUser();
        return userService.changePassword(user.getEmail(), oldPassword, newPassword);
    }
}