package ru.urfu.webapplication.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserRepository;
import ru.urfu.webapplication.service.EmailService;
import ru.urfu.webapplication.service.PaymentService;
import ru.urfu.webapplication.service.WeatherService;
import ru.urfu.webapplication.service.WeatherSubscriptionService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherSchedulerTest {

    @Mock
    private WeatherSubscriptionService weatherSubscriptionService;
    @Mock
    private WeatherService weatherService;
    @Mock
    private EmailService emailService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private WeatherScheduler scheduler;

    private UserSubscription subscription;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setApiKey("free-key");
        user.setSubscriptionLevel(SubscriptionLevel.BASIC);
        user.setIsActive(true);
        user.setAutoRenewal(false);
        user.setSubscriptionExpiresAt(LocalDateTime.now().minusDays(1));

        subscription = new UserSubscription();
        subscription.setId(1L);
        subscription.setCity("Moscow");
        subscription.setUser(user);
        subscription.setNotifyHeat(true);
        subscription.setNotifyCold(true);
        subscription.setNotifyWind(true);
        subscription.setNotifyPrecipitation(true);
    }

    //Проверяет рассылку погодных предупреждений
    @Test
    void sendDailyWeatherAlerts_ShouldSendEmail_WhenHeatAlert() {
        when(weatherSubscriptionService.getAllSubscriptions()).thenReturn(List.of(subscription));
        when(weatherService.checkWeatherConditions("Moscow", 1, "ru"))
                .thenReturn("Жара: 35.0 градусов");

        scheduler.sendDailyWeatherAlerts();

        verify(emailService, times(1)).sendWeatherAlertEmail("test@example.com", "Moscow", "Жара: 35.0 градусов");
    }

    //Проверяет: рассылка не отправляет письмо, если уведомления отключены
    @Test
    void sendDailyWeatherAlerts_ShouldNotSendEmail_WhenNotifyDisabled() {
        subscription.setNotifyHeat(false);
        when(weatherSubscriptionService.getAllSubscriptions()).thenReturn(List.of(subscription));
        when(weatherService.checkWeatherConditions("Moscow", 1, "ru"))
                .thenReturn("Жара: 35.0 градусов");

        scheduler.sendDailyWeatherAlerts();

        verify(emailService, never()).sendWeatherAlertEmail(anyString(), anyString(), anyString());
    }

    //Проверяет понижение подписки без автопродления
    @Test
    void processExpiredSubscriptions_ShouldDowngrade_WhenAutoRenewalDisabled() {
        when(userRepository.findBySubscriptionExpiresAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(user));

        scheduler.processExpiredSubscriptions();

        verify(paymentService, times(1)).subscriptionReduction("free-key");
        verify(paymentService, never()).processAutoRenewal(anyString());
    }

    //Проверяет: автопродление когда включено
    @Test
    void processExpiredSubscriptions_ShouldAutoRenew_WhenAutoRenewalEnabled() {
        user.setAutoRenewal(true);
        when(userRepository.findBySubscriptionExpiresAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(user));
        doNothing().when(paymentService).processAutoRenewal("test@example.com");

        scheduler.processExpiredSubscriptions();

        verify(paymentService, times(1)).processAutoRenewal("test@example.com");
        verify(paymentService, never()).subscriptionReduction(anyString());
    }

    //Проверяет понижение подписки при ошибке автопродления
    @Test
    void processExpiredSubscriptions_ShouldDowngrade_WhenAutoRenewalFails() {
        user.setAutoRenewal(true);
        when(userRepository.findBySubscriptionExpiresAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(user));
        doThrow(new RuntimeException("Payment failed")).when(paymentService).processAutoRenewal("test@example.com");

        scheduler.processExpiredSubscriptions();

        verify(paymentService, times(1)).subscriptionReduction("free-key");
    }

    //Проверяет отправку уведомления о скором истечении подписки
    @Test
    void notifyExpiringSoon_ShouldSendEmail() {
        user.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(2));
        when(userRepository.findBySubscriptionExpiresAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(user));

        scheduler.notifyExpiringSoon();

        verify(emailService, times(1)).sendSubscriptionExpiringSoonEmail(
                eq("test@example.com"),
                eq(SubscriptionLevel.BASIC),
                any(LocalDateTime.class),
                anyLong(),
                eq(false)
        );
    }

    //Проверяет: не отправляет для FREE-пользователей
    @Test
    void notifyExpiringSoon_ShouldNotSendEmail_ForFreeUser() {
        user.setSubscriptionLevel(SubscriptionLevel.FREE);
        when(userRepository.findBySubscriptionExpiresAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(user));

        scheduler.notifyExpiringSoon();

        verify(emailService, never()).sendSubscriptionExpiringSoonEmail(
                anyString(), any(), any(), anyLong(), anyBoolean());
    }
}