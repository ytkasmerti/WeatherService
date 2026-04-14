package ru.urfu.webapplication.service;

import org.springframework.stereotype.Service;
import ru.urfu.webapplication.model.SubscriptionLevel;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiKeyService {

    private final Map<String, SubscriptionLevel> keys = new ConcurrentHashMap<>();

    public ApiKeyService() {
        //Тестовые ключи
        keys.put("free-key-111", SubscriptionLevel.FREE);
        keys.put("basic-key-222", SubscriptionLevel.BASIC);
        keys.put("premium-key-333", SubscriptionLevel.PREMIUM);
    }

    public boolean isValidKey(String apiKey) {
        return keys.containsKey(apiKey);
    }

    public SubscriptionLevel getSubscriptionLevel(String apiKey) {
        return keys.get(apiKey);
    }

    //Генерация ключа для нового пользователя
    public String generateApiKey(String email, String plan) {
        String apiKey = UUID.randomUUID().toString().substring(0, 8);
        SubscriptionLevel level;
        switch (plan.toLowerCase()) {
            case "basic":
                level = SubscriptionLevel.BASIC;
                break;
            case "premium":
                level = SubscriptionLevel.PREMIUM;
                break;
            default:
                level = SubscriptionLevel.FREE;
        }
        keys.put(apiKey, level);
        return apiKey;
    }
}