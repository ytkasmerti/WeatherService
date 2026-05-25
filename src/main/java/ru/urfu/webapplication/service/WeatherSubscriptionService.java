package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.repository.UserSubscriptionRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherSubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;

    @Transactional
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

        subscriptionRepository.save(subscription);
        log.info("Пользователь {} подписался на уведомления о погоде в городе {}",
                subscription.getEmail(), subscription.getCity());
    }

    @Transactional
    public void unsubscribe(Long subscriptionId, String email) {
        UserSubscription subscription = subscriptionRepository.findByIdAndEmail(subscriptionId, email)
                .orElseThrow(() -> new RuntimeException("Подписка не найдена"));
        subscriptionRepository.delete(subscription);
        log.info("Пользователь {} отписался от уведомлений подписки {}", email, subscriptionId);
    }

    @Transactional
    public void unsubscribeAll(String email) {
        subscriptionRepository.deleteByEmail(email);
        log.info("Пользователь {} отписался от всех уведомлений", email);
    }

    public List<UserSubscription> getUserSubscriptions(String email) {
        return subscriptionRepository.findByEmail(email);
    }

    public List<UserSubscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }
}