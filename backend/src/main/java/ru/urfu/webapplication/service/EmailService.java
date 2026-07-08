package ru.urfu.webapplication.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.urfu.webapplication.model.SubscriptionLevel;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;
    private final JavaMailSender mailSender;

    public void sendApiKeyEmail(String toEmail) {
        String subject = "Регистрация Weather Service";
        String text = String.format("""
                Здравствуйте! Вы успешно зарегистрированы в Weather Service.
                Ваш текущий тариф: FREE (Бесплатный). Вы можете повысить тариф в личном кабинете.
                
                FREE тариф:
                - Цена: 0 руб/месяц
                - Лимит запросов в день: 10 запросов
                - Подписки на погодные предупреждения: недоступно
                Доступно на FREE:
                - Текущая погода по городу
                - Текущая погода по координатам
                
                Повышение тарифа:
                BASIC (500 руб/месяц):
                - 100 запросов в день
                - 1 подписка на погодные предупреждения
                - Прогноз на 15 дней
                - Исторические данные в конкретную дату не более чем 7 дней назад
                - Исторические данные за период не более чем 7 дней
                
                PREMIUM (1000 руб/месяц):
                - Безлимит запросов
                - 5 подписок на погодные предупреждения
                - Прогноз на 15 дней
                - Исторические данные в конкретную дату не более чем 5 лет назад
                - Исторические данные за период не более чем 8 месяцев
                - Погода в конкретное время
                - Почасовой прогноз
                - Фильтрация прогноза
                
                С уважением, Команда Weather Service.""");
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Письмо регистрации отправлено на {}", toEmail);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке письма регистрации: {}", e.getMessage());
            throw new RuntimeException("Не удалось отправить письмо на " + toEmail, e);
        }
    }

    public void sendPaymentSuccessEmail(String toEmail, SubscriptionLevel level) {
        String subject = "Оплата подписки Weather Service";
        String text = String.format("""
                Здравствуйте! Ваш платёж для оплаты подписки %s был успешно подтверждён.
                С уважением, команда Weather Service.""", level);
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

    public void sendPaymentFailedEmail(String toEmail, SubscriptionLevel level, int amount) {
        String subject = "Ошибка оплаты подписки Weather Service";
        String text = String.format("""
                Здравствуйте! Ваш платёж на сумму %d руб. для активации подписки %s был отклонён.
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
                Здравствуйте! Получено уведомление по Вашей подписке на погодные предупреждения.
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

    public void sendSubscriptionExpiringSoonEmail(String toEmail, SubscriptionLevel level, LocalDateTime expiresAt,
                                                  long daysUntilExpiry, boolean autoRenewal) {
        String subject = "Подписка Weather Service скоро истечёт";
        String autoRenewalStatus = autoRenewal ? "включено" : "отключено";
        String text = String.format("""
                        Здравствуйте! Ваша подписка %s истечёт %s (через %d дней).
                        Статус автопродления: %s
                        Для продления подписки вы можете:
                        1. Включить автопродление
                        2. Создать платёж вручную
                        С уважением, команда Weather Service.""",
                level, expiresAt.toString(), daysUntilExpiry, autoRenewalStatus);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Уведомление о скором истечении подписки отправлено на {}", toEmail);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке уведомления о скором истечении: {}", e.getMessage());
        }
    }

    public void sendSubscriptionExpiredEmail(String toEmail, SubscriptionLevel oldLevel) {
        String subject = "Срок действия подписки Weather Service истёк";
        String text = String.format("""
                Здравствуйте! Срок действия Вашей подписки %s истёк, уровень подписки понижен до FREE.
                Для восстановления подписки оформите новый платёж.
                С уважением, команда Weather Service.""", oldLevel);
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

    public void sendAutoRenewalSuccessEmail(String toEmail, SubscriptionLevel level, LocalDateTime newExpiryDate) {
        String subject = "Подписка Weather Service автоматически продлена";
        String text = String.format("""
                        Здравствуйте! Ваша подписка %s была автоматически продлена.
                        Новый срок действия: %s.
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

    public void sendAccountDeletedEmail(String toEmail) {
        String subject = "Аккаунт Weather Service удалён";
        String text = """
                Здравствуйте! Ваш аккаунт в Weather Service был успешно удалён.
                Если Вы не удаляли аккаунт, пожалуйста, свяжитесь с поддержкой.
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

    public void sendPasswordResetCode(String toEmail, String code) {
        String subject = "Восстановление пароля для Weather Service";
        String text = String.format("""
                Здравствуйте! Вы запросили восстановление пароля для аккаунта.
                Ваш код для сброса пароля: %s
                Если Вы не запрашивали восстановление пароля, просто проигнорируйте это письмо.
                С уважением, команда Weather Service.""", code);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Код восстановления пароля отправлен на {}", toEmail);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке кода восстановления: {}", e.getMessage());
            throw new RuntimeException("Не удалось отправить код на " + toEmail, e);
        }
    }

    public void sendPasswordChangedEmail(String toEmail) {
        String subject = "Пароль для Weather Service изменён";
        String text = """
                Здравствуйте! Ваш пароль для входа в Weather Service был успешно изменён.
                Если Вы не меняли пароль, пожалуйста, свяжитесь с поддержкой.
                С уважением, команда Weather Service.""";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Уведомление о смене пароля отправлено на {}", toEmail);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке уведомления о смене пароля: {}", e.getMessage());
        }
    }
}