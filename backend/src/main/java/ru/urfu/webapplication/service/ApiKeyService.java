package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserRepository;
import ru.urfu.webapplication.repository.WeatherRequestRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {
    private final UserRepository userRepository;
    private final WeatherRequestRepository requestRepository;
    @Value("${weather.limits.free}")
    private int freeLimit;
    @Value("${weather.limits.basic}")
    private int basicLimit;
    @Value("${weather.limits.premium}")
    private int premiumLimit;

    public int getMaxRequests(SubscriptionLevel level) {
        return switch (level) {
            case FREE -> freeLimit;
            case BASIC -> basicLimit;
            case PREMIUM -> premiumLimit == -1 ? Integer.MAX_VALUE : premiumLimit;
        };
    }

    public String generateApiKey(String email, SubscriptionLevel level) {
        String prefix = switch (level) {
            case FREE -> "free";
            case BASIC -> "basic";
            case PREMIUM -> "premium";
        };
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return prefix + "-" + uniqueId + "-" + Math.abs(email.hashCode());
    }

    public SubscriptionLevel validateAndGetLevel(User user) {
        if (!user.getIsActive()) {
            throw new RuntimeException("Неверный API ключ");
        }
        if (!canMakeRequest(user)) {
            throw new RuntimeException("Превышен лимит запросов на сегодня");
        }
        return user.getSubscriptionLevel();
    }

    public SubscriptionLevel validateAndGetLevel(String apiKey) {
        return validateAndGetLevel(getUserByApiKey(apiKey));
    }

    public boolean canMakeRequest(User user) {
        SubscriptionLevel level = user.getSubscriptionLevel();
        if (level == null) {
            return false;
        }
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        long requestCount = requestRepository.countRequestsByUserInLast24Hours(user, twentyFourHoursAgo);
        return requestCount < getMaxRequests(level);
    }

    public User getUserByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("Неверный API ключ"));
    }

    @Transactional
    public boolean deactivateKey(String apiKey) {
        return userRepository.findByApiKey(apiKey)
                .map(key -> {
                    key.setIsActive(false);
                    userRepository.save(key);
                    log.info("API key deactivated: {}", apiKey);
                    return true;
                })
                .orElse(false);
    }

    public String getEmailByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey)
                .map(User::getEmail)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}