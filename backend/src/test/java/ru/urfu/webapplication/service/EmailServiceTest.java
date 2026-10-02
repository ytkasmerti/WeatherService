package ru.urfu.webapplication.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import ru.urfu.webapplication.model.SubscriptionLevel;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private static final String TEST_EMAIL = "user@example.com";
    private static final String FROM_EMAIL = "noreply@test.com";

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", FROM_EMAIL);
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    //Проверяет отправку письма с API-ключом
    @Test
    void sendApiKeyEmail_ShouldSend() {
        emailService.sendApiKeyEmail(TEST_EMAIL);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    //Проверяет отправку письма об успешной оплате
    @Test
    void sendPaymentSuccessEmail_ShouldSend() {
        emailService.sendPaymentSuccessEmail(TEST_EMAIL, SubscriptionLevel.PREMIUM);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    //Проверяет отправку письма об отклонении платежа
    @Test
    void sendPaymentFailedEmail_ShouldSend() {
        emailService.sendPaymentFailedEmail(TEST_EMAIL, SubscriptionLevel.BASIC, 500);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    //Проверяет отправку погодного предупреждения
    @Test
    void sendWeatherAlertEmail_ShouldSend() {
        emailService.sendWeatherAlertEmail(TEST_EMAIL, "Moscow", "Жара: 35.0 градусов");

        verify(mailSender, times(1)).send(mimeMessage);
    }

    //Проверяет отправку уведомления о скором истечении подписки
    @Test
    void sendSubscriptionExpiringSoonEmail_ShouldSend() {
        emailService.sendSubscriptionExpiringSoonEmail(
                TEST_EMAIL,
                SubscriptionLevel.BASIC,
                LocalDateTime.now().plusDays(3),
                3,
                true
        );

        verify(mailSender, times(1)).send(mimeMessage);
    }

    //Проверяет отправку письма об истечении подписки
    @Test
    void sendSubscriptionExpiredEmail_ShouldSend() {
        emailService.sendSubscriptionExpiredEmail(TEST_EMAIL, SubscriptionLevel.PREMIUM);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    //Проверяет отправку письма об успешном продлении подписки
    @Test
    void sendAutoRenewalSuccessEmail_ShouldSend() {
        emailService.sendAutoRenewalSuccessEmail(
                TEST_EMAIL,
                SubscriptionLevel.BASIC,
                LocalDateTime.now().plusMonths(1)
        );

        verify(mailSender, times(1)).send(mimeMessage);
    }

    //Проверяет отправку письма об удалении аккаунта
    @Test
    void sendAccountDeletedEmail_ShouldSend() {
        emailService.sendAccountDeletedEmail(TEST_EMAIL);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    //Проверяет отправку кода восстановления пароля
    @Test
    void sendPasswordResetCode_ShouldSend() {
        emailService.sendPasswordResetCode(TEST_EMAIL, "123456");

        verify(mailSender, times(1)).send(mimeMessage);
    }

    //Проверяет отправку письма о смене пароля
    @Test
    void sendPasswordChangedEmail_ShouldSend() {
        emailService.sendPasswordChangedEmail(TEST_EMAIL);

        verify(mailSender, times(1)).send(mimeMessage);
    }
}