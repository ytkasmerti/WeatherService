package ru.urfu.webapplication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import ru.urfu.webapplication.dto.PaymentDto;
import ru.urfu.webapplication.dto.PaymentHistoryDto;
import ru.urfu.webapplication.entity.Payment;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.PaymentStatus;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.PaymentRepository;
import ru.urfu.webapplication.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String TEST_KEY = "free-test-key";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PAYMENT_ID = "p-1";
    private static final int AMOUNT_BASIC = 500;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ApiKeyService apiKeyService;
    @Mock
    private EmailService emailService;
    @Mock
    private DtoMapperService mapper;
    @Mock
    private WeatherSubscriptionService subscriptionService;

    @InjectMocks
    private PaymentService paymentService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "basicPrice", AMOUNT_BASIC);
        ReflectionTestUtils.setField(paymentService, "premiumPrice", 1000);
        ReflectionTestUtils.setField(paymentService, "successProbability", 0.8);
        ReflectionTestUtils.setField(paymentService, "paymentExpireMinutes", 15L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(TEST_EMAIL);
        testUser.setApiKey(TEST_KEY);
        testUser.setSubscriptionLevel(SubscriptionLevel.FREE);
        testUser.setIsActive(true);
    }

    //Проверяет создание платежа для BASIC
    @Test
    void createPayment_ShouldReturnPendingPayment_WhenValidRequest() {
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));
        when(paymentRepository.findByUserAndStatusAndExpiresAtAfter(
                eq(testUser), eq(PaymentStatus.PENDING), any()))
                .thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        PaymentDto expected = PaymentDto.builder()
                .paymentId(TEST_PAYMENT_ID).level(SubscriptionLevel.BASIC).amount(AMOUNT_BASIC).build();
        when(mapper.toPaymentDto(any(Payment.class), anyString())).thenReturn(expected);

        PaymentDto result = paymentService.createPayment(TEST_KEY, SubscriptionLevel.BASIC, false);

        assertNotNull(result);
        assertEquals(SubscriptionLevel.BASIC, result.getLevel());
        assertEquals(AMOUNT_BASIC, result.getAmount());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    //Проверяет: ошибка если у пользователя уже есть такая подписка
    @Test
    void createPayment_ShouldThrowException_WhenSameSubscriptionExists() {
        testUser.setSubscriptionLevel(SubscriptionLevel.BASIC);
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.createPayment(TEST_KEY, SubscriptionLevel.BASIC, false));
        assertTrue(ex.getMessage().contains("уже есть активная подписка"));
    }

    //Проверяет: ошибка если есть ожидающий платёж
    @Test
    void createPayment_ShouldThrowException_WhenPendingPaymentExists() {
        Payment pending = new Payment();
        pending.setPaymentId("pending-1");
        pending.setUser(testUser);
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));
        when(paymentRepository.findByUserAndStatusAndExpiresAtAfter(
                eq(testUser), eq(PaymentStatus.PENDING), any()))
                .thenReturn(Optional.of(pending));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.createPayment(TEST_KEY, SubscriptionLevel.BASIC, false));
        assertTrue(ex.getMessage().contains("уже есть ожидающий платеж"));
    }

    //Проверяет ошибку при тарифе FREE
    @Test
    void createPayment_ShouldThrowException_WhenLevelFree() {
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.createPayment(TEST_KEY, SubscriptionLevel.FREE, false));
        assertTrue(ex.getMessage().contains("Неверный тариф"));
    }

    //Проверяет: успешная оплата обновляет подписку
    @Test
    void confirmPayment_ShouldUpdateSubscription_WhenSuccess() {
        Payment payment = new Payment();
        payment.setPaymentId(TEST_PAYMENT_ID);
        payment.setUser(testUser);
        payment.setLevel(SubscriptionLevel.BASIC);
        payment.setAmount(AMOUNT_BASIC);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(paymentRepository.findByPaymentId(TEST_PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(apiKeyService.generateApiKey(TEST_EMAIL, SubscriptionLevel.BASIC)).thenReturn("basic-new-key");
        when(apiKeyService.deactivateKey(TEST_KEY)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        PaymentDto expected = PaymentDto.builder()
                .paymentId(TEST_PAYMENT_ID).apiKey("basic-new-key").status(PaymentStatus.CONFIRMED).build();
        when(mapper.toPaymentDto(any(Payment.class), anyString())).thenReturn(expected);

        PaymentService spy = spy(paymentService);
        doReturn(0.5).when(spy).getRandomValue();

        PaymentDto result = spy.confirmPayment(TEST_PAYMENT_ID, TEST_KEY);

        assertNotNull(result);
        assertEquals(PaymentStatus.CONFIRMED, result.getStatus());
        assertEquals(SubscriptionLevel.BASIC, testUser.getSubscriptionLevel());
        assertEquals("basic-new-key", testUser.getApiKey());
        verify(emailService, times(1)).sendPaymentSuccessEmail(anyString(), eq(SubscriptionLevel.BASIC));
    }

    //Проверяет: неуспешная оплата помечается FAILED
    @Test
    void confirmPayment_ShouldMarkFailed_WhenUnsuccessful() {
        Payment payment = new Payment();
        payment.setPaymentId(TEST_PAYMENT_ID);
        payment.setUser(testUser);
        payment.setLevel(SubscriptionLevel.BASIC);
        payment.setAmount(AMOUNT_BASIC);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(paymentRepository.findByPaymentId(TEST_PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        PaymentDto expected = PaymentDto.builder()
                .paymentId(TEST_PAYMENT_ID).status(PaymentStatus.FAILED).build();
        when(mapper.toPaymentDto(any(Payment.class), anyString())).thenReturn(expected);

        PaymentService spy = spy(paymentService);
        doReturn(0.9).when(spy).getRandomValue();

        PaymentDto result = spy.confirmPayment(TEST_PAYMENT_ID, TEST_KEY);

        assertEquals(PaymentStatus.FAILED, result.getStatus());
        verify(emailService, times(1)).sendPaymentFailedEmail(anyString(), eq(SubscriptionLevel.BASIC), eq(AMOUNT_BASIC));
    }

    //Проверяет: просроченный платёж помечается EXPIRED
    @Test
    void confirmPayment_ShouldReturnExpired_WhenExpired() {
        Payment payment = new Payment();
        payment.setPaymentId(TEST_PAYMENT_ID);
        payment.setUser(testUser);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setExpiresAt(LocalDateTime.now().minusMinutes(5));

        when(paymentRepository.findByPaymentId(TEST_PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        PaymentDto expected = PaymentDto.builder()
                .paymentId(TEST_PAYMENT_ID).status(PaymentStatus.EXPIRED).build();
        when(mapper.toPaymentDto(any(Payment.class), anyString())).thenReturn(expected);

        PaymentDto result = paymentService.confirmPayment(TEST_PAYMENT_ID, TEST_KEY);

        assertEquals(PaymentStatus.EXPIRED, result.getStatus());
    }

    //Проверяет: ошибка при уже FAILED платеже
    @Test
    void confirmPayment_ShouldThrowException_WhenAlreadyFailed() {
        Payment payment = new Payment();
        payment.setPaymentId(TEST_PAYMENT_ID);
        payment.setUser(testUser);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(paymentRepository.findByPaymentId(TEST_PAYMENT_ID)).thenReturn(Optional.of(payment));

        assertThrows(RuntimeException.class,
                () -> paymentService.confirmPayment(TEST_PAYMENT_ID, TEST_KEY));
    }

    //Проверяет статус платежа
    @Test
    void getPaymentStatus_ShouldReturnPayment() {
        Payment payment = new Payment();
        payment.setPaymentId(TEST_PAYMENT_ID);
        payment.setUser(testUser);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByPaymentId(TEST_PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(mapper.toPaymentDto(payment)).thenReturn(PaymentDto.builder().paymentId(TEST_PAYMENT_ID).build());

        PaymentDto result = paymentService.getPaymentStatus(TEST_PAYMENT_ID);

        assertEquals(TEST_PAYMENT_ID, result.getPaymentId());
    }

    //Проверяет: ошибка если платёж не найден
    @Test
    void getPaymentStatus_ShouldThrowException_WhenNotFound() {
        when(paymentRepository.findByPaymentId("bad")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.getPaymentStatus("bad"));
    }

    //Проверяет вывод истории платежей
    @Test
    void getPaymentHistoryByEmail_ShouldReturnHistory() {
        Page<Payment> emptyPage = new PageImpl<>(List.of());
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(paymentRepository.findAllByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(mapper.toPaymentHistoryDto(emptyPage))
                .thenReturn(PaymentHistoryDto.builder().totalCount(0).build());

        PaymentHistoryDto result = paymentService.getPaymentHistoryByEmail(TEST_EMAIL, 0, 20);

        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    //Проверяет: ошибка если пользователь не найден
    @Test
    void getPaymentHistoryByEmail_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> paymentService.getPaymentHistoryByEmail("nobody@example.com", 0, 20));
    }

    //Проверяет понижение подписки при истечении
    @Test
    void subscriptionReduction_ShouldDowngradeToFree_WhenExpired() {
        testUser.setSubscriptionLevel(SubscriptionLevel.BASIC);
        testUser.setSubscriptionExpiresAt(LocalDateTime.now().minusDays(1));

        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));
        when(apiKeyService.generateApiKey(TEST_EMAIL, SubscriptionLevel.FREE)).thenReturn("free-new-key");
        when(apiKeyService.deactivateKey(TEST_KEY)).thenReturn(true);

        paymentService.subscriptionReduction(TEST_KEY);

        assertEquals(SubscriptionLevel.FREE, testUser.getSubscriptionLevel());
        assertEquals("free-new-key", testUser.getApiKey());
        assertNull(testUser.getSubscriptionExpiresAt());
        verify(subscriptionService, times(1)).unsubscribeAll(TEST_EMAIL);
        verify(emailService, times(1)).sendSubscriptionExpiredEmail(TEST_EMAIL, SubscriptionLevel.BASIC);
    }

    //Проверяет: не понижает если подписка ещё активна
    @Test
    void subscriptionReduction_ShouldNotDowngrade_WhenNotExpired() {
        testUser.setSubscriptionLevel(SubscriptionLevel.BASIC);
        testUser.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(5));
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        paymentService.subscriptionReduction(TEST_KEY);

        assertEquals(SubscriptionLevel.BASIC, testUser.getSubscriptionLevel());
        verify(subscriptionService, never()).unsubscribeAll(anyString());
    }

    //Проверяет: не понижает если expiry == null
    @Test
    void subscriptionReduction_ShouldReturn_WhenExpiryNull() {
        testUser.setSubscriptionLevel(SubscriptionLevel.BASIC);
        testUser.setSubscriptionExpiresAt(null);
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        paymentService.subscriptionReduction(TEST_KEY);

        verify(userRepository, never()).save(any());
    }
}