package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.urfu.webapplication.entity.UserSubscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    //заменить на бд
    private final ConcurrentHashMap<String, UserSubscription> subscriptions = new ConcurrentHashMap<>();

    //подписаться
    public void subscribe(UserSubscription sub) {
        subscriptions.put(sub.getEmail(), sub);
        log.info("Пользователь {} подписался на уведомления о погоде в {}", sub.getEmail(), sub.getCity());
    }

    //отписаться
    public void unsubscribe(String email) {
        UserSubscription sub = subscriptions.get(email);
        if (sub == null) {
            throw new RuntimeException("У вас нет активной подписки на уведомления");
        }
        subscriptions.remove(email);
        log.info("Пользователь {} отписался от уведомлений", email);
    }

    //информация о подписке
    public UserSubscription getSubscription(String email) {
        UserSubscription sub = subscriptions.get(email);
        if (sub == null) {
            throw new RuntimeException("У вас нет активной подписки на уведомления");
        }
        return subscriptions.get(email);
    }

    //получение всех подписок
    public List<UserSubscription> getAllSubscriptions() {
        return new ArrayList<>(subscriptions.values());
    }
}