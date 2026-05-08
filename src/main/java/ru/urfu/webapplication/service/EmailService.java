package ru.urfu.webapplication.service;

import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;
    private final JavaMailSender mailSender;

    public void sendApiKeyEmail(String toEmail, String apiKey, String subscriptionLevel) {
        String subject = "Ваш API ключ для Weather Service";
        String text = String.format("""
                        Здравствуйте! Вы зарегистрированы в Weather Service.
                        Уровень подписки: %s. Ваш API ключ: %s
                        Сохраните этот ключ. Он понадобится для всех запросов к API.
                        С уважением, команда Weather Service.""",
                subscriptionLevel, apiKey, apiKey);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Письмо с API ключом отправлено на {}", toEmail);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке письма регистрации: {}", e.getMessage());
            throw new RuntimeException("Не удалось отправить письмо на " + toEmail, e);
        }
    }

    public void sendPaymentSuccessEmail(String toEmail, String newApiKey, String level) {
        String subject = "Оплата подписки Weather Service";
        String text = String.format("""
                        Здравствуйте! Ваш платеж успешно подтвержден.
                        Новый уровень подписки: %s. Ваш новый API ключ: %s
                        Сохраните этот ключ. Старый ключ больше не работает.
                        С уважением, команда Weather Service.""",
                level, newApiKey);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Письмо об успешной оплате отправлено на {}", toEmail);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке письма об успешной оплате: {}", e.getMessage());
            throw new RuntimeException("Не удалось отправить письмо об оплате на " + toEmail, e);
        }
    }
}