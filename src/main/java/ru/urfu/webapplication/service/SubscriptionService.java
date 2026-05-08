package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.urfu.webapplication.entity.UserSubscription;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    //заменить на бд
    private final ConcurrentHashMap<String, UserSubscription> subscriptions = new ConcurrentHashMap<>();

    public void subscribe(UserSubscription sub) {
        subscriptions.put(sub.getEmail(), sub);
        log.info("Пользователь {} подписался на уведомления о погоде в {}", sub.getEmail(), sub.getCity());
    }
}