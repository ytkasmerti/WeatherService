package ru.urfu.webapplication.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserRepository;
import ru.urfu.webapplication.repository.WeatherRequestRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_KEY = "basic-old-key";
    private static final String OLD_PASSWORD_HASH = "hashed";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private ApiKeyService apiKeyService;
    @Mock
    private WeatherRequestRepository requestRepository;
    @Mock
    private WeatherSubscriptionService subscriptionService;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(OLD_PASSWORD_HASH);
        testUser.setApiKey(TEST_KEY);
        testUser.setSubscriptionLevel(SubscriptionLevel.BASIC);
        testUser.setIsActive(true);
        testUser.setSubscriptionExpiresAt(LocalDateTime.now().minusDays(1));
    }

    //Проверяет понижение подписки при истечении
    @Test
    void subscriptionReduction_ShouldDowngradeToFree_WhenExpired() {
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));
        when(apiKeyService.generateApiKey(TEST_EMAIL, SubscriptionLevel.FREE)).thenReturn("free-new-key");
        when(apiKeyService.deactivateKey(TEST_KEY)).thenReturn(true);

        userService.subscriptionReduction(TEST_KEY);

        assertEquals(SubscriptionLevel.FREE, testUser.getSubscriptionLevel());
        assertEquals("free-new-key", testUser.getApiKey());
        assertNull(testUser.getSubscriptionExpiresAt());
        verify(subscriptionService, times(1)).unsubscribeAll(TEST_EMAIL);
        verify(emailService, times(1)).sendSubscriptionExpiredEmail(TEST_EMAIL, SubscriptionLevel.BASIC);
    }

    //Проверяет: не понижает если подписка ещё активна
    @Test
    void subscriptionReduction_ShouldNotDowngrade_WhenNotExpired() {
        testUser.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(5));
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        userService.subscriptionReduction(TEST_KEY);

        assertEquals(SubscriptionLevel.BASIC, testUser.getSubscriptionLevel());
        verify(subscriptionService, never()).unsubscribeAll(anyString());
    }

    //Проверяет: не понижает если expiry == null
    @Test
    void subscriptionReduction_ShouldReturn_WhenExpiryNull() {
        testUser.setSubscriptionExpiresAt(null);
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        userService.subscriptionReduction(TEST_KEY);

        verify(userRepository, never()).save(any());
    }

    //Проверяет профиль с лимитами для BASIC
    @Test
    void getProfile_ShouldReturnProfileWithLimits_WhenBasicUser() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(apiKeyService.getMaxRequests(SubscriptionLevel.BASIC)).thenReturn(100);
        when(requestRepository.countRequestsByUserInLast24Hours(eq(testUser), any()))
                .thenReturn(30L);

        Map<String, Object> result = userService.getProfile(TEST_EMAIL);

        assertEquals(TEST_EMAIL, result.get("email"));
        assertEquals(SubscriptionLevel.BASIC, result.get("subscriptionLevel"));

        @SuppressWarnings("unchecked")
        Map<String, Object> limits = (Map<String, Object>) result.get("limits");
        assertEquals(100, limits.get("dailyLimit"));
        assertEquals(30L, limits.get("usedToday"));
        assertEquals(70, limits.get("remainingToday"));
    }

    //Проверяет профиль с безлимитом для PREMIUM
    @Test
    void getProfile_ShouldReturnUnlimited_WhenPremiumUser() {
        testUser.setSubscriptionLevel(SubscriptionLevel.PREMIUM);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(apiKeyService.getMaxRequests(SubscriptionLevel.PREMIUM)).thenReturn(Integer.MAX_VALUE);

        Map<String, Object> result = userService.getProfile(TEST_EMAIL);

        @SuppressWarnings("unchecked")
        Map<String, Object> limits = (Map<String, Object>) result.get("limits");
        assertEquals("Неограничено", limits.get("dailyLimit"));
        assertEquals("Неограничено", limits.get("remainingToday"));
    }

    //Проверяет включение автопродления подписки
    @Test
    void setAutoRenewal_ShouldUpdateUser_WhenEnabled() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        Map<String, Object> result = userService.setAutoRenewal(TEST_EMAIL, true);

        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("autoRenewal"));
        assertTrue(testUser.getAutoRenewal());
    }

    //Проверяет отключение автопродления подписки
    @Test
    void setAutoRenewal_ShouldUpdateUser_WhenDisabled() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        Map<String, Object> result = userService.setAutoRenewal(TEST_EMAIL, false);

        assertEquals(false, result.get("autoRenewal"));
        assertFalse(testUser.getAutoRenewal());
    }

    //Проверяет успешное удаление аккаунта
    @Test
    void deleteAccount_ShouldDeleteUser_WhenPasswordValid() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", OLD_PASSWORD_HASH)).thenReturn(true);
        when(apiKeyService.deactivateKey(TEST_KEY)).thenReturn(true);

        Map<String, String> result = userService.deleteAccount(TEST_EMAIL, "password123", response);

        assertEquals("true", result.get("success"));
        verify(userRepository, times(1)).delete(testUser);
        verify(emailService, times(1)).sendAccountDeletedEmail(TEST_EMAIL);
        verify(response, times(1)).addCookie(any());
    }

    //Проверяет неверный пароль при удалении
    @Test
    void deleteAccount_ShouldReturnError_WhenPasswordInvalid() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", OLD_PASSWORD_HASH)).thenReturn(false);

        Map<String, String> result = userService.deleteAccount(TEST_EMAIL, "wrong", response);

        assertEquals("Неверный пароль", result.get("error"));
        verify(userRepository, never()).delete(any());
    }

    //Проверяет успешную смену пароля
    @Test
    void changePassword_ShouldUpdatePassword_WhenOldPasswordValid() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldpass", OLD_PASSWORD_HASH)).thenReturn(true);
        when(passwordEncoder.encode("newpass123")).thenReturn("new-hashed");

        Map<String, String> result = userService.changePassword(TEST_EMAIL, "oldpass", "newpass123");

        assertEquals("true", result.get("success"));
        assertEquals("new-hashed", testUser.getPassword());
        verify(emailService, times(1)).sendPasswordChangedEmail(TEST_EMAIL);
    }

    //Проверяет неверный старый пароль
    @Test
    void changePassword_ShouldThrowException_WhenOldPasswordInvalid() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", OLD_PASSWORD_HASH)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.changePassword(TEST_EMAIL, "wrong", "newpass123"));
        assertEquals("Неверный текущий пароль", ex.getMessage());
    }
}