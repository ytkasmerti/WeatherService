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
    private final Map<String, PaymentDto> payments = new ConcurrentHashMap<>(); //заменить на бд

    private int getPrice(String plan) {
        return switch (plan.toUpperCase()) {
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
                "currentPlan", user.getSubscriptionLevel().name(),
                "expiresAt", user.getSubscriptionExpiresAt() != null ? user.getSubscriptionExpiresAt().toString() : "не ограничено",
                "autoRenewal", user.getAutoRenewal() != null && user.getAutoRenewal(),
                "isActive", user.getIsActive()
        );
    }

    //Создание платежа
    public PaymentDto createPayment(String apiKey, String plan) {
        User user = userRepository.findByApiKey(apiKey).orElseThrow(()
                -> new RuntimeException("Пользователь не найден"));
        String planUpper = plan.toUpperCase();
        int price = getPrice(planUpper);

        if (price == 0) {
            throw new RuntimeException("Неверный тариф. Доступны: BASIC, PREMIUM");
        }

        if (user.getSubscriptionLevel().name().equals(planUpper)) {
            throw new RuntimeException("У вас уже есть подписка " + planUpper);
        }

        String paymentId = UUID.randomUUID().toString();
        PaymentDto payment = PaymentDto.builder()
                .paymentId(paymentId)
                .apiKey(apiKey)
                .plan(planUpper)
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

        SubscriptionLevel newLevel = SubscriptionLevel.valueOf(payment.getPlan());
        user.setSubscriptionLevel(newLevel);
        user.setSubscriptionExpiresAt(LocalDateTime.now().plusMonths(1));
        userRepository.save(user);

        log.info("Пользователь {} повышен до {}", payment.getApiKey(), newLevel);
        return payment;
    }
}