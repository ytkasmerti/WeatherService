package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.webapplication.entity.ApiKey;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.ApiKeyRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    @Transactional
    // регистрация пользователя
    public Map<String, String> registerUser(String email, String plan) {
        SubscriptionLevel level;
        try {
            level = SubscriptionLevel.valueOf(plan.toUpperCase());
        } catch (IllegalArgumentException e) {
            level = SubscriptionLevel.FREE;
        }

        if (apiKeyRepository.findByEmail(email).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Email already registered");
            response.put("message", "This email already has an API key");
            return response;
        }

        String keyValue = generateApiKey(email, level);

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyValue(keyValue);
        apiKey.setEmail(email);
        apiKey.setSubscriptionLevel(level);
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setIsActive(true);

        apiKeyRepository.save(apiKey);

        Map<String, String> response = new HashMap<>();
        response.put("apiKey", keyValue);
        response.put("subscriptionLevel", level.name());
        response.put("message", "Successfully registered! Save your API key.");

        log.info("New user registered: {} with {} plan", email, level);
        return response;
    }
    // проверка уровня подписки
    public SubscriptionLevel getSubscriptionLevel(String apiKey) {
        return apiKeyRepository.findByKeyValueAndIsActiveTrue(apiKey)
                .map(ApiKey::getSubscriptionLevel)
                .orElse(null);
    }
    // проверка может ли пользователь делать запросы
    public boolean canMakeRequest(String apiKey) {
        SubscriptionLevel level = getSubscriptionLevel(apiKey);
        if (level == null) {
            return false;
        }

        int maxRequests = switch (level) {
            case FREE -> 10;
            case BASIC -> 100;
            case PREMIUM -> Integer.MAX_VALUE;
        };

        if (maxRequests == Integer.MAX_VALUE) {
            return true;
        }

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        long requestCount = apiKeyRepository.countRequestsByKeyInLast24Hours(apiKey, twentyFourHoursAgo);

        return requestCount < maxRequests;
    }

    // блокировка ключа
    @Transactional
    public boolean deactivateKey(String apiKey) {
        return apiKeyRepository.findByKeyValueAndIsActiveTrue(apiKey)
                .map(key -> {
                    key.setIsActive(false);
                    apiKeyRepository.save(key);
                    log.info("API key deactivated: {}", apiKey);
                    return true;
                })
                .orElse(false);
    }
    // создание API ключа
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