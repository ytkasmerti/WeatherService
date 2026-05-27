package ru.urfu.webapplication.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    //curl -X POST "http://localhost:8080/auth/login?email=weatherservice26@gmail.com&password=123456" -c cookies.txt
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam @NotBlank String email,
                                     @RequestParam @NotBlank String password,
                                     HttpServletResponse response) {
        log.info("Попытка входа пользователя {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден: {}", email);
                    return new RuntimeException("Неверный email или пароль");
                });
        //Проверяем пароль
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (!matches) {
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

    //curl -X POST "http://localhost:8080/auth/logout" -b cookies.txt
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