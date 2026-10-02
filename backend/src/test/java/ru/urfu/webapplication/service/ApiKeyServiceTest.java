package ru.urfu.webapplication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserRepository;
import ru.urfu.webapplication.repository.WeatherRequestRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    private static final String TEST_KEY = "free-test-key";
    private static final String TEST_EMAIL = "test@example.com";

    @Mock
    private UserRepository userRepository;
    @Mock
    private WeatherRequestRepository requestRepository;

    @InjectMocks
    private ApiKeyService apiKeyService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apiKeyService, "freeLimit", 10);
        ReflectionTestUtils.setField(apiKeyService, "basicLimit", 100);
        ReflectionTestUtils.setField(apiKeyService, "premiumLimit", -1);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(TEST_EMAIL);
        testUser.setApiKey(TEST_KEY);
        testUser.setSubscriptionLevel(SubscriptionLevel.FREE);
        testUser.setIsActive(true);
    }

    //Проверяет успешная валидацию ключа
    @Test
    void validateAndGetLevel_ShouldReturnLevel_WhenKeyValid() {
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));
        when(requestRepository.countRequestsByKeyInLast24Hours(eq(TEST_KEY), any()))
                .thenReturn(5L);

        SubscriptionLevel level = apiKeyService.validateAndGetLevel(TEST_KEY);

        assertEquals(SubscriptionLevel.FREE, level);
    }

    //Проверяет невалидный ключе
    @Test
    void validateAndGetLevel_ShouldThrowException_WhenKeyInvalid() {
        when(userRepository.findByApiKey("bad-key")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> apiKeyService.validateAndGetLevel("bad-key"));
        assertEquals("Неверный API ключ", ex.getMessage());
    }

    //Проверяет превышение лимита запросов
    @Test
    void validateAndGetLevel_ShouldThrowException_WhenLimitExceeded() {
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));
        when(requestRepository.countRequestsByKeyInLast24Hours(eq(TEST_KEY), any()))
                .thenReturn(10L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> apiKeyService.validateAndGetLevel(TEST_KEY));
        assertEquals("Превышен лимит запросов на сегодня", ex.getMessage());
    }

    //Проверяет: isValidKey возвращает true для активного
    @Test
    void isValidKey_ShouldReturnTrue_WhenActive() {
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        assertTrue(apiKeyService.isValidKey(TEST_KEY));
    }

    //Проверяет: isValidKey возвращает false для неактивного
    @Test
    void isValidKey_ShouldReturnFalse_WhenInactive() {
        testUser.setIsActive(false);
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        assertFalse(apiKeyService.isValidKey(TEST_KEY));
    }

    //Проверяет: isValidKey false для несуществующего
    @Test
    void isValidKey_ShouldReturnFalse_WhenNotFound() {
        when(userRepository.findByApiKey("bad-key")).thenReturn(Optional.empty());

        assertFalse(apiKeyService.isValidKey("bad-key"));
    }

    //Проверяет: getSubscriptionLevel возвращает уровень
    @Test
    void getSubscriptionLevel_ShouldReturnLevel_WhenFound() {
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        assertEquals(SubscriptionLevel.FREE, apiKeyService.getSubscriptionLevel(TEST_KEY));
    }

    //Проверяет: getSubscriptionLevel null если нет
    @Test
    void getSubscriptionLevel_ShouldReturnNull_WhenNotFound() {
        when(userRepository.findByApiKey("bad-key")).thenReturn(Optional.empty());

        assertNull(apiKeyService.getSubscriptionLevel("bad-key"));
    }

    //Проверяет: canMakeRequest true если лимит не превышен
    @Test
    void canMakeRequest_ShouldReturnTrue_WhenUnderLimit() {
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));
        when(requestRepository.countRequestsByKeyInLast24Hours(eq(TEST_KEY), any()))
                .thenReturn(5L);

        assertTrue(apiKeyService.canMakeRequest(TEST_KEY));
    }

    //Проверяет: canMakeRequest false если лимит превышен
    @Test
    void canMakeRequest_ShouldReturnFalse_WhenLimitReached() {
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));
        when(requestRepository.countRequestsByKeyInLast24Hours(eq(TEST_KEY), any()))
                .thenReturn(10L);

        assertFalse(apiKeyService.canMakeRequest(TEST_KEY));
    }

    //Проверяет успешную деактивацию ключа
    @Test
    void deactivateKey_ShouldReturnTrue_WhenKeyFound() {
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        boolean result = apiKeyService.deactivateKey(TEST_KEY);

        assertTrue(result);
        assertFalse(testUser.getIsActive());
        verify(userRepository, times(1)).save(testUser);
    }

    //Проверяет: деактивация false если ключа нет
    @Test
    void deactivateKey_ShouldReturnFalse_WhenKeyNotFound() {
        when(userRepository.findByApiKey("bad-key")).thenReturn(Optional.empty());

        assertFalse(apiKeyService.deactivateKey("bad-key"));
    }

    //Проверяет: getEmailByApiKey возвращает email
    @Test
    void getEmailByApiKey_ShouldReturnEmail_WhenFound() {
        when(userRepository.findByApiKey(TEST_KEY)).thenReturn(Optional.of(testUser));

        assertEquals(TEST_EMAIL, apiKeyService.getEmailByApiKey(TEST_KEY));
    }

    //Проверяет: getEmailByApiKey кидает ошибку если нет
    @Test
    void getEmailByApiKey_ShouldThrowException_WhenNotFound() {
        when(userRepository.findByApiKey("bad-key")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> apiKeyService.getEmailByApiKey("bad-key"));
    }
}