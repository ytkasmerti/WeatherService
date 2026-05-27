package ru.urfu.webapplication.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserRepository;
import ru.urfu.webapplication.service.ApiKeyService;
import ru.urfu.webapplication.service.EmailService;
import ru.urfu.webapplication.service.WeatherSubscriptionService;
import ru.urfu.webapplication.service.WeatherService;

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
    private final ApiKeyService apiKeyService;

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

        for (User user : expiredUsers) {
            if (user.getSubscriptionLevel() != SubscriptionLevel.FREE) {
                log.info("Подписка пользователя {} просрочена, понижаем до FREE", user.getEmail());

                String oldLevel = user.getSubscriptionLevel().name();

                String newApiKey = apiKeyService.generateApiKey(user.getEmail(), "FREE");

                apiKeyService.deactivateKey(user.getApiKey());

                user.setApiKey(newApiKey);
                user.setSubscriptionLevel(SubscriptionLevel.FREE);
                user.setSubscriptionExpiresAt(null);
                user.setAutoRenewal(false);
                userRepository.save(user);

                log.info("Подписка пользователя {} понижена с {} до FREE. Новый ключ: {}",
                        user.getEmail(), oldLevel, newApiKey);

                emailService.sendSubscriptionExpiredEmail(user.getEmail(), oldLevel, newApiKey);
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

                String autoRenewalStatus = Boolean.TRUE.equals(user.getAutoRenewal()) ? "включено" : "отключено";
                String message = String.format("""
                        Ваша подписка %s истечёт %s (через %d дней).
                        Статус автопродления: %s
                        Для продления подписки вы можете:
                        1. Включить автопродление
                        2. Создать платёж вручную""",
                        user.getSubscriptionLevel().name(),
                        user.getSubscriptionExpiresAt().toString(),
                        daysUntilExpiry,
                        autoRenewalStatus);

                emailService.sendCustomEmail(user.getEmail(), "Подписка скоро истечёт", message);
            }
        }
    }
}