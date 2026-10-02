package ru.urfu.webapplication.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import ru.urfu.webapplication.entity.PasswordResetCode;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.PasswordResetCodeRepository;
import ru.urfu.webapplication.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_KEY = "free-test-key";
    private static final String HASHED_PASSWORD = "hashed-password";
    private static final String RESET_CODE = "123456";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordResetCodeRepository passwordResetCodeRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private ApiKeyService apiKeyService;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "resetCodeExpireMinutes", 15);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(HASHED_PASSWORD);
        testUser.setApiKey(TEST_KEY);
        testUser.setSubscriptionLevel(SubscriptionLevel.FREE);
        testUser.setIsActive(true);
    }

    //Проверяет успешную регистрацию
    @Test
    void registerUser_ShouldReturnApiKey_WhenValidData() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(apiKeyService.generateApiKey("new@example.com", SubscriptionLevel.FREE)).thenReturn("free-new-key");
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        Map<String, String> result = authService.registerUser("new@example.com", "password123", "password123");

        assertEquals("free-new-key", result.get("apiKey"));
        assertEquals("FREE", result.get("subscriptionLevel"));
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendApiKeyEmail("new@example.com");
    }

    //Проверяет несовпадение паролей
    @Test
    void registerUser_ShouldReturnError_WhenPasswordsDontMatch() {
        Map<String, String> result = authService.registerUser("new@example.com", "pass1", "pass2");

        assertEquals("Пароли не совпадают", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    //Проверяет зарегистрированный email при регистрации
    @Test
    void registerUser_ShouldReturnError_WhenEmailExists() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        Map<String, String> result = authService.registerUser(TEST_EMAIL, "pass123", "pass123");

        assertEquals("Email уже зарегистрирован", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    //Проверяет успешный вход
    @Test
    void login_ShouldReturnUserData_WhenCredentialsValid() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", HASHED_PASSWORD)).thenReturn(true);

        Map<String, Object> result = authService.login(TEST_EMAIL, "password123", response);

        assertEquals(true, result.get("success"));
        assertEquals(TEST_EMAIL, result.get("email"));
        assertEquals("FREE", result.get("subscriptionLevel"));
        verify(response, times(1)).setHeader(eq("Set-Cookie"), anyString());
    }

    //Проверяет неверный пароль
    @Test
    void login_ShouldThrowException_WhenPasswordInvalid() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", HASHED_PASSWORD)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(TEST_EMAIL, "wrong", response));
        assertEquals("Неверный email или пароль", ex.getMessage());
    }

    //Проверяет несуществующего пользователя
    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("nobody@example.com", "pass", response));
        assertEquals("Неверный email или пароль", ex.getMessage());
    }

    //Проверяет: logout очищает куки
    @Test
    void logout_ShouldClearCookie() {
        Map<String, String> result = authService.logout(response);

        assertEquals("Вы вышли из системы", result.get("message"));
        verify(response, times(1)).addCookie(any());
    }

    //Проверяет: checkAuth с null
    @Test
    void checkAuth_ShouldReturnNotAuthenticated_WhenApiKeyNull() {
        Map<String, Object> result = authService.checkAuth(null);

        assertEquals(false, result.get("authenticated"));
    }

    //Проверяет: checkAuth с валидным ключом
    @Test
    void checkAuth_ShouldReturnAuthenticated_WhenApiKeyValid() {
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        Map<String, Object> result = authService.checkAuth(TEST_KEY);

        assertEquals(true, result.get("authenticated"));
        assertEquals(TEST_EMAIL, result.get("email"));
    }

    //Проверяет: checkAuth с неактивным пользователем
    @Test
    void checkAuth_ShouldReturnNotAuthenticated_WhenUserInactive() {
        testUser.setIsActive(false);
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        Map<String, Object> result = authService.checkAuth(TEST_KEY);

        assertEquals(false, result.get("authenticated"));
    }

    //Проверяет: forgotPassword генерирует код
    @Test
    void forgotPassword_ShouldGenerateCodeAndSendEmail() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordResetCodeRepository.save(any(PasswordResetCode.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Map<String, String> result = authService.forgotPassword(TEST_EMAIL);

        assertEquals(TEST_EMAIL, result.get("email"));
        verify(passwordResetCodeRepository, times(1)).deleteByEmail(TEST_EMAIL);
        verify(passwordResetCodeRepository, times(1)).save(any(PasswordResetCode.class));
    }

    //Проверяет: forgotPassword кидает ошибку если юзера нет
    @Test
    void forgotPassword_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.forgotPassword("nobody@example.com"));
    }

    //Проверяет успешный сброс пароля
    @Test
    void resetPassword_ShouldUpdatePassword_WhenCodeValid() {
        PasswordResetCode resetCode = new PasswordResetCode();
        resetCode.setEmail(TEST_EMAIL);
        resetCode.setCode(RESET_CODE);

        when(passwordResetCodeRepository.findByEmailAndCodeAndExpiresAtAfter(
                eq(TEST_EMAIL), eq(RESET_CODE), any(LocalDateTime.class)))
                .thenReturn(Optional.of(resetCode));
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpass123")).thenReturn("new-hashed");

        Map<String, String> result = authService.resetPassword(TEST_EMAIL, RESET_CODE, "newpass123");

        assertEquals("true", result.get("success"));
        verify(userRepository, times(1)).save(testUser);
        verify(passwordResetCodeRepository, times(1)).deleteByEmail(TEST_EMAIL);
        verify(emailService, times(1)).sendPasswordChangedEmail(TEST_EMAIL);
    }

    //Проверяет неверный код сброса пароля
    @Test
    void resetPassword_ShouldThrowException_WhenCodeInvalid() {
        when(passwordResetCodeRepository.findByEmailAndCodeAndExpiresAtAfter(anyString(), anyString(), any()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.resetPassword(TEST_EMAIL, "wrong", "newpass123"));
        assertEquals("Неверный или просроченный код подтверждения", ex.getMessage());
    }
}