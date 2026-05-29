package ru.urfu.webapplication.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.service.AuthService;
import ru.urfu.webapplication.service.UserService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    //curl -X POST "http://localhost:8080/auth/login?email=weatherservice26@gmail.com&password=123456" -c cookies.txt
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam @NotBlank String email,
                                     @RequestParam @NotBlank String password,
                                     HttpServletResponse response) {
        return authService.login(email, password, response);
    }

    //curl -X POST "http://localhost:8080/auth/logout" -b cookies.txt
    @GetMapping("/logout")
    public Map<String, String> logout(HttpServletResponse response) {
        return authService.logout(response);
    }

    @GetMapping("/check")
    public Map<String, Object> checkAuth(@CookieValue(value = "apiKey", required = false) String apiKey) {
        return authService.checkAuth(apiKey);
    }

    //Запрос на сброс и восстановление пароля
    //POST http://localhost:8080/auth/forgot-password?email=weatherservice26@gmail.com
    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestParam @NotBlank String email) {
        return userService.forgotPassword(email);
    }

    //Подтверждение сброса пароля
    //POST http://localhost:8080/auth/reset-password?email=weatherservice26@gmail.com&code=&newPassword=123456
    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(
            @RequestParam @NotBlank String email,
            @RequestParam @NotBlank String code,
            @RequestParam @NotBlank @Size(min = 6, message = "Пароль должен содержать минимум 6 символов") String newPassword) {
        return userService.resetPassword(email, code, newPassword);
    }
}