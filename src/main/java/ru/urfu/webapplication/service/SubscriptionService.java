package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserRepository userRepository;
    //заменить на бд
    private final ConcurrentHashMap<Long, UserSubscription> subscriptions = new ConcurrentHashMap<>();
    private Long nextId = 1L;

    //Получить уровень подписки пользователя по email
    private SubscriptionLevel getUserLevel(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getSubscriptionLevel())
                .orElse(SubscriptionLevel.FREE);
    }

    //Получить количество подписок пользователя
    private int getSubscriptionsCount(String email) {
        return (int) subscriptions.values().stream()
                .filter(sub -> sub.getEmail().equals(email))
                .count();
    }

    //подписаться (BASIC - 1 подписка, PREMIUM- до 5 подписок)
    @PreAuthorize("hasRole('BASIC') or hasRole('PREMIUM')")
    public void subscribe(UserSubscription sub) {
        SubscriptionLevel level = getUserLevel(sub.getEmail());
        int currentCount = getSubscriptionsCount(sub.getEmail());
        if (level == SubscriptionLevel.BASIC && currentCount >= 1) {
            throw new RuntimeException("BASIC подписка позволяет иметь только 1 подписку на уведомления");
        }
        if (level == SubscriptionLevel.PREMIUM && currentCount >= 5) {
            throw new RuntimeException("Вы достигли максимального количества подписок на уведомления");
        }
        sub.setId(nextId++);
        subscriptions.put(sub.getId(), sub);
        log.info("Пользователь {} подписался на уведомления о погоде в {}", sub.getEmail(), sub.getCity());
    }

    //отписаться от конкретной по id
    public void unsubscribe(Long subscriptionId, String email) {
        UserSubscription sub = subscriptions.get(subscriptionId);
        if (sub == null) {
            throw new RuntimeException("Подписка с id " + subscriptionId + " не найдена");
        }
        if (!sub.getEmail().equals(email)) {
            throw new RuntimeException("Эта подписка принадлежит другому пользователю");
        }
        subscriptions.remove(subscriptionId);
        log.info("Пользователь {} отписался от уведомлений для города {}", email, sub.getCity());
    }

    //Отписаться от всех уведомлений
    public void unsubscribeAll(String email) {
        List<Long> idsToRemove = subscriptions.values().stream()
                .filter(sub -> sub.getEmail().equals(email))
                .map(UserSubscription::getId)
                .collect(Collectors.toList());
        if (idsToRemove.isEmpty()) {
            throw new RuntimeException("У вас нет активных подписок на уведомления");
        }
        idsToRemove.forEach(subscriptions::remove);
        log.info("Пользователь {} отписался от всех уведомлений", email);
    }

    //Получить все подписки пользователя
    public List<UserSubscription> getUserSubscriptions(String email) {
        return subscriptions.values().stream()
                .filter(sub -> sub.getEmail().equals(email))
                .collect(Collectors.toList());
    }

    //получение всех подписок
    public List<UserSubscription> getAllSubscriptions() {
        return new ArrayList<>(subscriptions.values());
    }
}