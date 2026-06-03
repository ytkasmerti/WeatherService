package ru.urfu.webapplication.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.service.AuthService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Регистрация
    @PostMapping("/register")
    public Map<String, String> register(@RequestParam @NotBlank String email,
                                        @RequestParam @NotBlank
                                        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов") String password,
                                        @RequestParam @NotBlank String confirmPassword) {
        return authService.registerUser(email, password, confirmPassword);
    }

    //вход с куки
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam @NotBlank String email,
                                     @RequestParam @NotBlank String password,
                                     HttpServletResponse response) {
        return authService.login(email, password, response);
    }

    //выход из аккаунта
    @GetMapping("/logout")
    public Map<String, String> logout(HttpServletResponse response) {
        return authService.logout(response);
    }

    @GetMapping("/check")
    public Map<String, Object> checkAuth(@CookieValue(value = "apiKey", required = false) String apiKey) {
        return authService.checkAuth(apiKey);
    }

    //Запрос на сброс и восстановление пароля
    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestParam @NotBlank String email) {
        return authService.forgotPassword(email);
    }

    //Подтверждение сброса пароля
    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(
            @RequestParam @NotBlank String email,
            @RequestParam @NotBlank String code,
            @RequestParam @NotBlank @Size(min = 6, message = "Пароль должен содержать минимум 6 символов") String newPassword) {
        return authService.resetPassword(email, code, newPassword);
    }
}