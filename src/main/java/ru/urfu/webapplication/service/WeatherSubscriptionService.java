package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.webapplication.annotation.RequireBasicOrPremium;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserRepository;
import ru.urfu.webapplication.repository.UserSubscriptionRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherSubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ru.urfu.webapplication.client.VisualCrossingClient visualCrossingClient;

    //Подписаться (BASIC - 1 подписка, PREMIUM- до 5 подписок)
    @Transactional
    @RequireBasicOrPremium
    public void subscribe(String apiKey, String city, boolean notifyHeat, boolean notifyCold,
                          boolean notifyWind, boolean notifyPrecipitation) {
        try {
            visualCrossingClient.getCurrentWeather(city, "ru");
        } catch (Exception e) {
            log.error("Попытка подписки на несуществующий город: {}", city);
            throw new RuntimeException("Город '" + city + "' не найден. Проверьте правильность написания.");
        }
        User user = userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (subscriptionRepository.existsByUserAndCity(user, city)) {
            log.warn("Пользователь {} уже подписан на город {}", user.getEmail(), city);
            List<UserSubscription> existing = subscriptionRepository.findByUser(user);
            for (UserSubscription sub : existing) {
                if (sub.getCity().equals(city)) {
                    sub.setNotifyHeat(notifyHeat);
                    sub.setNotifyCold(notifyCold);
                    sub.setNotifyWind(notifyWind);
                    sub.setNotifyPrecipitation(notifyPrecipitation);
                    subscriptionRepository.save(sub);
                    return;
                }
            }
        }
        int currentCount = subscriptionRepository.findByUser(user).size();
        SubscriptionLevel level = user.getSubscriptionLevel();

        if (level == SubscriptionLevel.BASIC && currentCount >= 1) {
            throw new RuntimeException("BASIC подписка позволяет иметь только 1 подписку");
        }
        if (level == SubscriptionLevel.PREMIUM && currentCount >= 5) {
            throw new RuntimeException("PREMIUM подписка позволяет иметь не более 5 подписок");
        }

        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user);
        subscription.setCity(city);
        subscription.setNotifyHeat(notifyHeat);
        subscription.setNotifyCold(notifyCold);
        subscription.setNotifyWind(notifyWind);
        subscription.setNotifyPrecipitation(notifyPrecipitation);
        subscriptionRepository.save(subscription);
        log.info("Пользователь {} подписался на уведомления о погоде в городе {}", user.getEmail(), city);
    }

    //Отписаться по айди подписки
    @Transactional
    public void unsubscribe(Long subscriptionId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        UserSubscription subscription = subscriptionRepository.findByIdAndUser(subscriptionId, user)
                .orElseThrow(() -> new RuntimeException("Подписка не найдена"));
        subscriptionRepository.delete(subscription);
        log.info("Пользователь {} отписался от уведомлений подписки {}", email, subscriptionId);
    }

    //Отписаться от всех подписок
    @Transactional
    public void unsubscribeAll(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        subscriptionRepository.deleteByUser(user);
        log.info("Пользователь {} отписался от всех уведомлений", email);
    }

    //Получить все подписки пользователя
    public List<UserSubscription> getUserSubscriptions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return subscriptionRepository.findByUser(user);
    }

    //Получить все подписки всех пользователей
    public List<UserSubscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }
}