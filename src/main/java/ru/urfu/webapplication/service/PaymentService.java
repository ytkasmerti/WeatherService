package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.urfu.webapplication.dto.PaymentDto;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    //заменить на бд
    private final Map<String, PaymentDto> payments = new ConcurrentHashMap<>();
    private final ApiKeyService apiKeyService;
    private final EmailService emailService;

    private int getPrice(String level) {
        return switch (level.toUpperCase()) {
            case "BASIC" -> 500;
            case "PREMIUM" -> 1000;
            default -> 0;
        };
    }

    //Информация о подписке
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

    //Создание платежа
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
        String paymentId = UUID.randomUUID().toString();
        PaymentDto payment = PaymentDto.builder()
                .paymentId(paymentId)
                .apiKey(apiKey)
                .level(levelUpper)
                .amount(price)
                .createdAt(LocalDateTime.now())
                .build();
        payments.put(paymentId, payment);
        log.info("Создан платеж {} для {} на сумму {}", paymentId, apiKey, price);
        return payment;
    }

    //Подтверждение платежа
    public PaymentDto confirmPayment(String paymentId, String apiKey) {
        PaymentDto payment = payments.get(paymentId);
        if (payment == null) {
            throw new RuntimeException("Платёж не найден");
        }
        if (!payment.getApiKey().equals(apiKey)) {
            throw new RuntimeException("Неверный API ключ для этого платежа");
        }
        User user = userRepository.findByApiKey(payment.getApiKey()).orElseThrow(()
                -> new RuntimeException("Пользователь не найден"));

        //Имитация оплаты (успешная оплата 80/20)
        double random = Math.random();
        boolean paymentSuccess = random < 0.8;
        if (!paymentSuccess) {
            log.warn("Платеж {} отклонен", paymentId);
            payments.remove(paymentId);
            throw new RuntimeException("Оплата отклонена банком. Попробуйте другую карту или повторите позже.");
        }
        //Имитация времени обработки платежа
        log.warn("Платеж {} в обработке", paymentId);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.warn("Платеж {} принят", paymentId);
        SubscriptionLevel newLevel = SubscriptionLevel.valueOf(payment.getLevel());
        //Генерируем новый ключ с новым уровнем и блокируем старый
        String newApiKey = apiKeyService.generateApiKey(user.getEmail(), payment.getLevel());
        apiKeyService.deactivateKey(apiKey);

        //Обновляем пользователя
        user.setApiKey(newApiKey);
        user.setSubscriptionLevel(newLevel);
        user.setSubscriptionExpiresAt(LocalDateTime.now().plusMonths(1));
        user.setIsActive(true);
        userRepository.save(user);
        payment.setApiKey(newApiKey);
        payments.remove(paymentId);
        log.info("Подписка у {} обновлена до подписки {}, новый ключ: {}", user.getEmail(), payment.getLevel(), newApiKey);

        //Отправка письма
        emailService.sendPaymentSuccessEmail(user.getEmail(), newApiKey, payment.getLevel());
        return payment;
    }
}