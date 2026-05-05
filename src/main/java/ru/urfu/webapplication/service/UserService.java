package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Map<String, String> registerUser(String email, String plan) {
        SubscriptionLevel level;
        try {
            level = SubscriptionLevel.valueOf(plan.toUpperCase());
        } catch (IllegalArgumentException e) {
            level = SubscriptionLevel.FREE;
        }

        if (userRepository.findByEmail(email).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Email уже зарегестрирован");
            response.put("message", "Этот email уже существует");
            return response;
        }

        String apiKey = generateApiKey(email, level);

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(apiKey));
        user.setApiKey(apiKey);
        user.setSubscriptionLevel(level);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);

        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("apiKey", apiKey);
        response.put("subscriptionLevel", level.name());
        response.put("message", "Регистрация прошла успешно! Сохраните ваш API ключ!");

        log.info("Новый пользователь успешно зарегистрирован: {} с {} планом", email, level);
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
}