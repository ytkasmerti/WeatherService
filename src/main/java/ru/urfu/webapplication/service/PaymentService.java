package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.webapplication.dto.PaymentDto;
import ru.urfu.webapplication.entity.Payment;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.PaymentRepository;
import ru.urfu.webapplication.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ApiKeyService apiKeyService;
    private final EmailService emailService;

    private int getPrice(String level) {
        return switch (level.toUpperCase()) {
            case "BASIC" -> 500;
            case "PREMIUM" -> 1000;
            default -> 0;
        };
    }

    // Информация о подписке
    public Map<String, Object> getSubscriptionInfo(String apiKey) {
        User user = userRepository.findByApiKey(apiKey).orElseThrow(()
                -> new RuntimeException("Пользователь не найден"));
        return Map.of(
                "apiKey", apiKey,
                "currentLevel", user.getSubscriptionLevel().name(),
                "expiresAt", user.getSubscriptionExpiresAt() != null ? user.getSubscriptionExpiresAt().toString() : "не ограничено",
                "autoRenewal", user.getAutoRenewal() != null && user.getAutoRenewal(),
                "isActive", user.getIsActive()
        );
    }

    // Создание платежа
    @Transactional
    public PaymentDto createPayment(String apiKey, String level) {
        User user = userRepository.findByApiKey(apiKey).orElseThrow(()
                -> new RuntimeException("Пользователь не найден"));

        String levelUpper = level.toUpperCase();
        int price = getPrice(levelUpper);
        if (price == 0) {
            throw new RuntimeException("Неверный тариф. Доступны: BASIC, PREMIUM");
        }
        if (user.getSubscriptionLevel().name().equals(levelUpper)) {
            throw new RuntimeException("У вас уже есть подписка " + levelUpper);
        }

        // Проверяем, нет ли уже ожидающего платежа
        paymentRepository.findByApiKeyAndIsConfirmedFalse(apiKey)
                .ifPresent(p -> {
                    throw new RuntimeException("У вас уже есть ожидающий платеж. PaymentId: " + p.getPaymentId());
                });

        String paymentId = UUID.randomUUID().toString();

        // Сохраняем в БД
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setApiKey(apiKey);
        payment.setEmail(user.getEmail());
        payment.setLevel(levelUpper);
        payment.setAmount(price);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setIsConfirmed(false);
        payment.setStatus("PENDING");

        paymentRepository.save(payment);

        log.info("Создан платеж {} для {} на сумму {}", paymentId, apiKey, price);

        return PaymentDto.builder()
                .paymentId(paymentId)
                .apiKey(apiKey)
                .level(levelUpper)
                .amount(price)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Подтверждение платежа
    @Transactional
    public PaymentDto confirmPayment(String paymentId, String apiKey) {
        // Ищем платеж в БД
        Payment payment = paymentRepository.findByPaymentIdAndApiKey(paymentId, apiKey)
                .orElseThrow(() -> new RuntimeException("Платёж не найден"));

        if (payment.getIsConfirmed()) {
            throw new RuntimeException("Платёж уже был подтверждён");
        }

        User user = userRepository.findByApiKey(payment.getApiKey()).orElseThrow(()
                -> new RuntimeException("Пользователь не найден"));

        // Имитация оплаты (успешная оплата 80/20)
        double random = Math.random();
        boolean paymentSuccess = random < 0.8;

        if (!paymentSuccess) {
            log.warn("Платеж {} отклонен", paymentId);
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
            emailService.sendPaymentFailedEmail(user.getEmail(), payment.getLevel(), payment.getAmount());
            throw new RuntimeException("Оплата отклонена банком. Попробуйте другую карту или повторите позже.");
        }

        // Имитация времени обработки платежа
        log.warn("Платеж {} в обработке", paymentId);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.warn("Платеж {} принят", paymentId);

        // Обновляем платеж
        payment.setIsConfirmed(true);
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setStatus("CONFIRMED");
        paymentRepository.save(payment);

        SubscriptionLevel newLevel = SubscriptionLevel.valueOf(payment.getLevel());

        // Генерируем новый ключ с новым уровнем и блокируем старый
        String newApiKey = apiKeyService.generateApiKey(user.getEmail(), payment.getLevel());
        apiKeyService.deactivateKey(apiKey);

        // Обновляем пользователя
        user.setApiKey(newApiKey);
        user.setSubscriptionLevel(newLevel);
        user.setSubscriptionExpiresAt(LocalDateTime.now().plusMonths(1));
        user.setIsActive(true);
        userRepository.save(user);

        log.info("Подписка у {} обновлена до подписки {}, новый ключ: {}", user.getEmail(), payment.getLevel(), newApiKey);

        // Отправка письма
        emailService.sendPaymentSuccessEmail(user.getEmail(), newApiKey, payment.getLevel());

        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .apiKey(newApiKey)
                .level(payment.getLevel())
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .message(String.format("Оплата успешна! Подписка %s активирована до %s. Проверьте почту для получения актуального API-ключа",
                        payment.getLevel(), user.getSubscriptionExpiresAt().toString()))
                .build();
    }

    @Transactional
    public boolean autoRenewSubscription(User user) {
        if (user.getSubscriptionExpiresAt() == null) {
            return false;
        }

        if (!Boolean.TRUE.equals(user.getAutoRenewal())) {
            log.info("Автопродление отключено для пользователя {}", user.getEmail());
            return false;
        }

        SubscriptionLevel currentLevel = user.getSubscriptionLevel();
        if (currentLevel == SubscriptionLevel.FREE) {
            log.info("Бесплатная подписка не требует продления для {}", user.getEmail());
            return false;
        }

        try {
            int price = getPrice(currentLevel.name());

            // Имитация списания средств (80% успеха)
            double random = Math.random();
            boolean paymentSuccess = random < 0.8;

            if (!paymentSuccess) {
                log.warn("Автопродление для {} не удалось - ошибка списания", user.getEmail());
                emailService.sendPaymentFailedEmail(user.getEmail(), currentLevel.name(), price);
                return false;
            }

            // Продлевание подписки на месяц
            LocalDateTime newExpiryDate = user.getSubscriptionExpiresAt().plusMonths(1);
            user.setSubscriptionExpiresAt(newExpiryDate);
            userRepository.save(user);

            log.info("Подписка {} автоматически продлена для {} до {}",
                    currentLevel, user.getEmail(), newExpiryDate);

            // Отправление уведомления об успешном продлении
            emailService.sendAutoRenewalSuccessEmail(user.getEmail(), currentLevel.name(), newExpiryDate);

            return true;

        } catch (Exception e) {
            log.error("Ошибка при автопродлении для {}: {}", user.getEmail(), e.getMessage());
            return false;
        }
    }
}