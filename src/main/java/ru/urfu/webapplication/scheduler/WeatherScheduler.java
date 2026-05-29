package ru.urfu.webapplication.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserRepository;
import ru.urfu.webapplication.service.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class WeatherScheduler {

    private final WeatherSubscriptionService weatherSubscriptionService;
    private final WeatherService weatherService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PaymentService paymentService;

    //Каждый день в 08:00 - "0 0 8 * * *"  (каждую минуту - "0 * * * * *")
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyWeatherAlerts() {
        log.info("Запуск ежедневной рассылки погодных уведомлений");
        List<UserSubscription> subscriptions = weatherSubscriptionService.getAllSubscriptions();
        for (UserSubscription sub : subscriptions) {
            String alertMessage = weatherService.checkWeatherConditions(sub.getCity(), 1, "ru");
            if (alertMessage != null) {
                boolean shouldSend = false;
                if (alertMessage.contains("Жара") && sub.isNotifyHeat()) {
                    shouldSend = true;
                } else if (alertMessage.contains("мороз") && sub.isNotifyCold()) {
                    shouldSend = true;
                } else if (alertMessage.contains("ветер") && sub.isNotifyWind()) {
                    shouldSend = true;
                } else if (alertMessage.contains("Осадки") && sub.isNotifyPrecipitation()) {
                    shouldSend = true;
                }
                if (shouldSend) {
                    emailService.sendWeatherAlertEmail(sub.getEmail(), sub.getCity(), alertMessage);
                }
            }
        }
    }

    // Проверка просроченных подписок каждый час
    @Scheduled(cron = "0 0 * * * *") // Каждый час
    public void processExpiredSubscriptions() {
        log.info("Запуск обработки просроченных подписок");

        LocalDateTime now = LocalDateTime.now();

        List<User> expiredUsers = userRepository.findBySubscriptionExpiresAtBefore(now);
        log.info("Просроченных пользователей: {}", expiredUsers.size());
        for (User user : expiredUsers) {
            if (user.getSubscriptionLevel() != SubscriptionLevel.FREE) {
                if (Boolean.TRUE.equals(user.getAutoRenewal())) {
                    log.info("Автопродление включено у пользователя {}", user.getEmail());
                    try {
                        paymentService.processAutoRenewal(user.getEmail());
                        log.info("Автопродление для пользователя {} удалось", user.getEmail());
                    } catch (Exception e) {
                        log.error("Автопродление для пользователя {} не удалось: {}", user.getEmail(), e.getMessage());
                        userService.subscriptionReduction(user.getApiKey());
                    }
                } else {
                    log.info("Автопродление выключено для пользователя {}, понижаем до FREE", user.getEmail());
                    userService.subscriptionReduction(user.getApiKey());
                }
            }
        }

    }

    // уведомление о скором истечении подписки (за 3 дня)
    @Scheduled(cron = "0 0 12 * * *") // Каждый день в 12:00
    public void notifyExpiringSoon() {
        log.info("Запуск проверки подписок, истекающих через 3 дня");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in3Days = now.plusDays(3);
        LocalDateTime endOfIn3Days = in3Days.withHour(23).withMinute(59).withSecond(59);

        List<User> users = userRepository.findBySubscriptionExpiresAtBetween(now, endOfIn3Days);

        for (User user : users) {
            if (user.getSubscriptionLevel() != SubscriptionLevel.FREE) {
                long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(now, user.getSubscriptionExpiresAt());
                log.info("Подписка пользователя {} истекает через {} дней", user.getEmail(), daysUntilExpiry);

                emailService.sendSubscriptionExpiringSoonEmail(
                        user.getEmail(),
                        user.getSubscriptionLevel().name(),
                        user.getSubscriptionExpiresAt(),
                        daysUntilExpiry,
                        Boolean.TRUE.equals(user.getAutoRenewal())
                );
            }
        }
    }
}