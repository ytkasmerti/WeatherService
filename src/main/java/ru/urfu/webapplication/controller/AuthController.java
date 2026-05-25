package ru.urfu.webapplication.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.repository.UserRepository;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @GetMapping("/login")
    public Map<String, Object> login(@RequestParam String apiKey, HttpServletResponse response) {
        User user = userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("Неверный API ключ"));

        if (!user.getIsActive()) {
            throw new RuntimeException("API ключ деактивирован");
        }

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

    @GetMapping("/logout")
    public Map<String, String> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("apiKey", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return Map.of("message", "Вы вышли из системы");
    }

    @GetMapping("/check")
    public Map<String, Object> checkAuth(@CookieValue(value = "apiKey", required = false) String apiKey) {
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
}