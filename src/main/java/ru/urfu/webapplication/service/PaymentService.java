package ru.urfu.webapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.webapplication.dto.PaymentDto;
import ru.urfu.webapplication.dto.PaymentHistoryDto;
import ru.urfu.webapplication.entity.Payment;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.PaymentStatus;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.PaymentRepository;
import ru.urfu.webapplication.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ApiKeyService apiKeyService;
    private final EmailService emailService;
    private final DtoMapperService mapper;
    @Value("${payment.price.basic}")
    private int basicPrice;
    @Value("${payment.price.premium}")
    private int premiumPrice;
    @Value("${payment.success-probability}")
    private double successProbability;
    @Value("${payment.expire-minutes}")
    private long paymentExpireMinutes;

    private int getPrice(String level) {
        return switch (level.toUpperCase()) {
            case "BASIC" -> basicPrice;
            case "PREMIUM" -> premiumPrice;
            default -> 0;
        };
    }

    //Создание платежа
    @Transactional
    public PaymentDto createPayment(String apiKey, SubscriptionLevel level, boolean isAutoRenewal) {
        User user = userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        SubscriptionLevel levelUpper = level;
        int price = getPrice(levelUpper.name());

        if (price == 0) {
            throw new RuntimeException("Неверный тариф. Доступны: BASIC, PREMIUM");
        }

        if (level.getPriority() < user.getSubscriptionLevel().getPriority()) {
            throw new RuntimeException("Нельзя сменить подписку на более низкий уровень: " + level);
        }

        if (!isAutoRenewal && user.getSubscriptionLevel() == levelUpper) {
            throw new RuntimeException("У вас уже есть активная подписка " + levelUpper);
        }

        paymentRepository.findByApiKeyAndStatusAndExpiresAtAfter(apiKey, PaymentStatus.PENDING, LocalDateTime.now())
                .ifPresent(p -> {
                    throw new RuntimeException("У вас уже есть ожидающий платеж. PaymentId: " + p.getPaymentId() +
                            ", истекает: " + p.getExpiresAt());
                });

        String paymentId = UUID.randomUUID().toString();

        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setApiKey(apiKey);
        payment.setUser(user);
        payment.setLevel(levelUpper);
        payment.setAmount(price);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(paymentExpireMinutes));
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);
        log.info("Создан платеж {} для {} на сумму {} рублей", paymentId, apiKey, price);
        String message = String.format("Платеж на сумму %d рублей создан. Совершите оплату в течение 15 минут.", payment.getAmount());
        return mapper.toPaymentDto(payment, apiKey, message);
    }

    //Подтверждение платежа
    @Transactional
    public PaymentDto confirmPayment(String paymentId, String apiKey) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Платеж не найден"));

        if (payment.getExpiresAt() != null && payment.getExpiresAt().isBefore(LocalDateTime.now())) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            return mapper.toPaymentDto(payment, apiKey, "Платеж просрочен. Создайте новый платеж и попробуйте снова.");
        }

        if (payment.getStatus().equals(PaymentStatus.CONFIRMED)) {
            log.info("Платеж {} уже был подтвержден ранее", paymentId);
            return mapper.toPaymentDto(payment, apiKey, "Платеж уже был подтвержден");
        }

        if (payment.getStatus().equals(PaymentStatus.FAILED)) {
            log.info("Платеж {} уже был отклонен ранее", paymentId);
            throw new RuntimeException("Платеж был отклонен. Создайте новый платеж.");
        }

        User user = userRepository.findByApiKey(payment.getApiKey())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        paymentRepository.save(payment);

        //Имитация оплаты
        double random = Math.random();
        boolean paymentSuccess = random < successProbability;
        if (!paymentSuccess) {
            log.warn("Платеж {} отклонен", paymentId);
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            emailService.sendPaymentFailedEmail(user.getEmail(), payment.getLevel(), payment.getAmount());
            return mapper.toPaymentDto(payment, apiKey, "Платеж отклонен банком. Попробуйте снова или повторите операцию позднее.");
        }

        //Имитация обработки платежа
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Платеж {} подтвержден", paymentId);
        payment.setStatus(PaymentStatus.CONFIRMED);
        payment.setConfirmedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        SubscriptionLevel newLevel = payment.getLevel();
        String newApiKey = apiKeyService.generateApiKey(user.getEmail(), payment.getLevel());
        apiKeyService.deactivateKey(apiKey);

        user.setApiKey(newApiKey);
        user.setSubscriptionLevel(newLevel);
        user.setSubscriptionExpiresAt(LocalDateTime.now().plusMonths(1));
        user.setIsActive(true);
        userRepository.save(user);

        emailService.sendPaymentSuccessEmail(user.getEmail(), newApiKey, payment.getLevel());
        String message = String.format("Оплата успешна! Подписка %s активирована до %s. Пожалуйста, перезайдите в свой аккаунт.",
                payment.getLevel(), user.getSubscriptionExpiresAt().toString());
        return mapper.toPaymentDto(payment, newApiKey, message);
    }

    //Проверка статуса платежа
    public PaymentDto getPaymentStatus(String paymentId) {
        log.info("Получение статуса платежа {}", paymentId);
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Платёж не найден"));
        return mapper.toPaymentDto(payment);
    }

    //Получить историю платежей по email
    public PaymentHistoryDto getPaymentHistoryByEmail(String email, int page, int size) {
        log.info("Пользователь {} запросил историю своих платежей, страница {}, размер {}", email, page, size);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с email " + email + " не найден"));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Payment> paymentsPage = paymentRepository.findAllByUserOrderByCreatedAtDesc(user, pageable);
        return mapper.toPaymentHistoryDto(paymentsPage);
    }

    //Автопродление подписки
    @Transactional
    public void processAutoRenewal(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        SubscriptionLevel level = user.getSubscriptionLevel();
        log.info("Начало автопродления для {}", email);

        PaymentDto payment = createPayment(user.getApiKey(), level, true);
        confirmPayment(payment.getPaymentId(), user.getApiKey());

        Payment paymentEntity = paymentRepository.findByPaymentId(payment.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Платеж не найден"));
        PaymentDto confirmResult = confirmPayment(payment.getPaymentId(), user.getApiKey());
        if (paymentEntity.getStatus() == PaymentStatus.FAILED || paymentEntity.getStatus() == PaymentStatus.EXPIRED) {
            log.error("Автоплатеж для пользователя {} не прошел: {}", email, confirmResult.getMessage());
            throw new RuntimeException("Платеж не прошел");
        }
        emailService.sendAutoRenewalSuccessEmail(user.getEmail(), level, user.getSubscriptionExpiresAt());
        log.info("Автопродление для {} успешно", email);
    }
}