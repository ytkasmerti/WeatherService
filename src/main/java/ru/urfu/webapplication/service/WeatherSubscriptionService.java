package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserSubscriptionRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherSubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final UserService userService;

    //подписаться (BASIC - 1 подписка, PREMIUM- до 5 подписок)
    @Transactional
    @PreAuthorize("hasRole('BASIC') or hasRole('PREMIUM')")
    public void subscribe(UserSubscription subscription) {
        // Проверяем, нет ли уже подписки на этот город
        if (subscriptionRepository.existsByEmailAndCity(subscription.getEmail(), subscription.getCity())) {
            log.warn("Пользователь {} уже подписан на город {}", subscription.getEmail(), subscription.getCity());
            // Обновляем существующую подписку
            List<UserSubscription> existing = subscriptionRepository.findByEmail(subscription.getEmail());
            for (UserSubscription sub : existing) {
                if (sub.getCity().equals(subscription.getCity())) {
                    sub.setNotifyHeat(subscription.isNotifyHeat());
                    sub.setNotifyCold(subscription.isNotifyCold());
                    sub.setNotifyWind(subscription.isNotifyWind());
                    sub.setNotifyPrecipitation(subscription.isNotifyPrecipitation());
                    subscriptionRepository.save(sub);
                    return;
                }
            }
        }
        // Проверка лимита подписок
        int currentCount = subscriptionRepository.findByEmail(subscription.getEmail()).size();
        SubscriptionLevel level = userService.findByEmail(subscription.getEmail()).getSubscriptionLevel();

        if (level == SubscriptionLevel.BASIC && currentCount >= 1) {
            throw new RuntimeException("BASIC подписка позволяет иметь только 1 подписку");
        }
        if (level == SubscriptionLevel.PREMIUM && currentCount >= 5) {
            throw new RuntimeException("PREMIUM подписка позволяет иметь не более 5 подписок");
        }

        subscriptionRepository.save(subscription);
        log.info("Пользователь {} подписался на уведомления о погоде в городе {}",
                subscription.getEmail(), subscription.getCity());

    }

    //Отписаться по айди подписки
    @Transactional
    public void unsubscribe(Long subscriptionId, String email) {
        UserSubscription subscription = subscriptionRepository.findByIdAndEmail(subscriptionId, email)
                .orElseThrow(() -> new RuntimeException("Подписка не найдена"));
        subscriptionRepository.delete(subscription);
        log.info("Пользователь {} отписался от уведомлений подписки {}", email, subscriptionId);
    }

    //Отписаться от всех подписок
    @Transactional
    public void unsubscribeAll(String email) {
        subscriptionRepository.deleteByEmail(email);
        log.info("Пользователь {} отписался от всех уведомлений", email);
    }

    //Получить все подписки пользователя
    public List<UserSubscription> getUserSubscriptions(String email) {
        return subscriptionRepository.findByEmail(email);
    }

    //Получить все подписки всех пользователей
    public List<UserSubscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }
}