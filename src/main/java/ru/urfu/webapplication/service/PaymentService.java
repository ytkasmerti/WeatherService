package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.repository.UserRepository;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;

    //Информация о подписке пользователя
    public Map<String, Object> getSubscriptionInfo(String apiKey) {
        User user = userRepository.findByApiKey(apiKey).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return Map.of(
                "apiKey", apiKey,
                "currentPlan", user.getSubscriptionLevel().name(),
                "expiresAt", user.getSubscriptionExpiresAt() != null ? user.getSubscriptionExpiresAt().toString() : "не ограничено",
                "autoRenewal", user.getAutoRenewal() != null && user.getAutoRenewal(),
                "isActive", user.getIsActive()
        );
    }
}