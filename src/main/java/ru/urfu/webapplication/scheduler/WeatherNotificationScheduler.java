package ru.urfu.webapplication.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.service.EmailService;
import ru.urfu.webapplication.service.SubscriptionService;
import ru.urfu.webapplication.service.WeatherService;

import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class WeatherNotificationScheduler {

    private final SubscriptionService subscriptionService;
    private final WeatherService weatherService;
    private final EmailService emailService;

    //Каждый день в 08:00 - "0 0 8 * * *"  (каждую минуту - "0 * * * * *")
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyWeatherAlerts() {
        log.info("Запуск ежедневной рассылки погодных уведомлений");
        List<UserSubscription> subscriptions = subscriptionService.getAllSubscriptions();
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
}