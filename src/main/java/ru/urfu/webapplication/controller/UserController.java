package ru.urfu.webapplication.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.repository.UserRepository;
import ru.urfu.webapplication.security.WeatherUserDetails;
import ru.urfu.webapplication.service.UserService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof WeatherUserDetails userDetails) {
            String email = userDetails.getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        }
        throw new RuntimeException("Пользователь не авторизован");
    }

    //Получение профиля пользователя
    @GetMapping("/profile")
    public Map<String, Object> getProfile() {
        User user = getCurrentUser();
        return userService.getProfile(user.getEmail());
    }

    // Включить/отключить автопродление подписки
    @GetMapping("/auto-renewal")
    public Map<String, Object> setAutoRenewal(@RequestParam boolean enabled) {
        User user = getCurrentUser();
        return userService.setAutoRenewal(user.getEmail(), enabled);
    }

    //Удаление аккаунта с подтверждением пароля
    @DeleteMapping("/delete")
    public Map<String, String> deleteAccount(@RequestParam @NotBlank String password, HttpServletResponse response) {
        User user = getCurrentUser();
        return userService.deleteAccount(user.getEmail(), password, response);
    }

    //Смена пароля пользователя
    @PostMapping("/change-password")
    public Map<String, String> changePassword(
            @RequestParam @NotBlank String oldPassword,
            @RequestParam @NotBlank @Size(min = 6, message = "Пароль должен содержать минимум 6 символов") String newPassword) {
        User user = getCurrentUser();
        return userService.changePassword(user.getEmail(), oldPassword, newPassword);
    }
}