package ru.urfu.webapplication.service;

import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;
    private final JavaMailSender mailSender;

    public void sendApiKeyEmail(String toEmail, String apiKey, String subscriptionLevel) {
        String subject = "Регистрация Weather Service";
        String text = String.format("""
                Здравствуйте! Вы зарегистрированы в Weather Service.
                Уровень подписки: %s. Ваш API ключ: %s.
                Вы можете повысить уровень подписки на сайте для доступа к большему количеству возможностей.
                С уважением, команда Weather Service.""", subscriptionLevel, apiKey, apiKey);
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
                С уважением, команда Weather Service.""", level, newApiKey);
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
            throw new RuntimeException("Не удалось отправить письмо об успешной оплате на " + toEmail, e);
        }
    }

    public void sendPaymentFailedEmail(String toEmail, String level, int amount) {
        String subject = "Ошибка оплаты подписки Weather Service";
        String text = String.format("""
                Здравствуйте! Ваш платеж на сумму %d руб. для активации подписки %s был отклонён.
                Попробуйте использовать другую карту или повторить попытку позже.
                С уважением, команда Weather Service.""", amount, level);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Письмо об отказе в оплате отправлено на {}", toEmail);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке письма об отказе в оплате: {}", e.getMessage());
        }
    }

    public void sendWeatherAlertEmail(String toEmail, String city, String alertMessage) {
        String subject = "Погодное предупреждение для " + city;
        String text = String.format("""
                Здравствуйте! Получено погодное уведомление.
                По городу %s обнаружено погодное явление:%s
                С уважением, команда Weather Service.""", city, alertMessage);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Погодное уведомление отправлено на {}", toEmail);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке погодного уведомления: {}", e.getMessage());
        }
    }

    public void sendSubscriptionExpiredEmail(String toEmail, String oldLevel, String newApiKey) {
        String subject = "Срок действия подписки истёк";
        String text = String.format("""
                Здравствуйте! Срок действия вашей подписки %s истёк, уровень подписки понижен до FREE.
                Ваш новый API ключ: %s
                Для восстановления подписки оформите новый платёж.
                С уважением, команда Weather Service.""", oldLevel, newApiKey);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Письмо об истечении подписки отправлено на {}", toEmail);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке письма об истечении подписки: {}", e.getMessage());
        }
    }

    public void sendAutoRenewalSuccessEmail(String toEmail, String level, LocalDateTime newExpiryDate) {
        String subject = "Подписка Weather Service автоматически продлена";
        String text = String.format("""
                        Здравствуйте! Ваша подписка %s была автоматически продлена.
                        Новый срок действия: %s
                        Спасибо, что остаётесь с нами!
                        С уважением, команда Weather Service.""",
                level, newExpiryDate.toString());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Уведомление об автопродлении отправлено на {}", toEmail);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке уведомления об автопродлении: {}", e.getMessage());
        }
    }

    public void sendCustomEmail(String toEmail, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Письмо отправлено на {}", toEmail);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке письма: {}", e.getMessage());
        }
    }

    public void sendAccountDeletedEmail(String toEmail) {
        String subject = "Аккаунт Weather Service удалён";
        String text = """
                Здравствуйте! Ваш аккаунт в Weather Service был успешно удалён.
                Если вы не удаляли аккаунт, пожалуйста, свяжитесь с поддержкой.
                С уважением, команда Weather Service.""";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Письмо об удалении аккаунта отправлено на {}", toEmail);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке письма об удалении аккаунта: {}", e.getMessage());
        }
    }
}