package ru.urfu.webapplication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.urfu.webapplication.client.VisualCrossingClient;
import ru.urfu.webapplication.dto.visualcrossingapi.CurrentConditions;
import ru.urfu.webapplication.dto.visualcrossingapi.VisualCrossingResponse;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserRepository;
import ru.urfu.webapplication.repository.UserSubscriptionRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherSubscriptionServiceTest {

    private static final String BASIC_KEY = "basic-key";
    private static final String PREMIUM_KEY = "premium-key";
    private static final String BASIC_EMAIL = "basic@example.com";
    private static final String PREMIUM_EMAIL = "premium@example.com";
    private static final String TEST_CITY = "Moscow";
    private static final String TEST_LANG = "ru";

    @Mock
    private UserSubscriptionRepository subscriptionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VisualCrossingClient visualCrossingClient;

    @InjectMocks
    private WeatherSubscriptionService subscriptionService;

    private User basicUser;
    private User premiumUser;
    private VisualCrossingResponse mockApiResponse;


    private UserSubscription createSub(String city) {
        UserSubscription sub = new UserSubscription();
        sub.setId((long) city.hashCode());
        sub.setCity(city);
        return sub;
    }

    @BeforeEach
    void setUp() {
        basicUser = new User();
        basicUser.setId(1L);
        basicUser.setEmail(BASIC_EMAIL);
        basicUser.setApiKey(BASIC_KEY);
        basicUser.setSubscriptionLevel(SubscriptionLevel.BASIC);

        premiumUser = new User();
        premiumUser.setId(2L);
        premiumUser.setEmail(PREMIUM_EMAIL);
        premiumUser.setApiKey(PREMIUM_KEY);
        premiumUser.setSubscriptionLevel(SubscriptionLevel.PREMIUM);

        mockApiResponse = new VisualCrossingResponse();
        CurrentConditions current = new CurrentConditions();
        current.setTemp(20.0);
        mockApiResponse.setCurrentConditions(current);
    }

    //Проверяет успешную подписку для BASIC
    @Test
    void subscribe_ShouldCreateSubscription_WhenBasicAndFirst() {
        when(visualCrossingClient.getCurrentWeather(TEST_CITY, TEST_LANG)).thenReturn(mockApiResponse);
        when(userRepository.findByApiKey(BASIC_KEY)).thenReturn(Optional.of(basicUser));
        when(subscriptionRepository.existsByUserAndCity(basicUser, TEST_CITY)).thenReturn(false);
        when(subscriptionRepository.findByUser(basicUser)).thenReturn(List.of());

        subscriptionService.subscribe(BASIC_KEY, TEST_CITY, true, true, true, true);

        verify(subscriptionRepository, times(1)).save(any(UserSubscription.class));
    }

    //Проверяет подписку на несуществующий город
    @Test
    void subscribe_ShouldThrowException_WhenCityInvalid() {
        when(visualCrossingClient.getCurrentWeather("InvalidCity", TEST_LANG))
                .thenThrow(new RuntimeException("API error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> subscriptionService.subscribe(BASIC_KEY, "InvalidCity", true, true, true, true));
        assertTrue(ex.getMessage().contains("не найден"));
    }

    //Проверяет: BASIC не может иметь > 1 подписки
    @Test
    void subscribe_ShouldThrowException_WhenBasicLimitExceeded() {
        when(visualCrossingClient.getCurrentWeather("Sochi", TEST_LANG)).thenReturn(mockApiResponse);
        when(userRepository.findByApiKey(BASIC_KEY)).thenReturn(Optional.of(basicUser));
        when(subscriptionRepository.existsByUserAndCity(basicUser, "Sochi")).thenReturn(false);

        UserSubscription existing = new UserSubscription();
        existing.setCity(TEST_CITY);
        when(subscriptionRepository.findByUser(basicUser)).thenReturn(List.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> subscriptionService.subscribe(BASIC_KEY, "Sochi", true, true, true, true));
        assertEquals("BASIC подписка позволяет иметь только 1 подписку", ex.getMessage());
    }

    //Проверяет: PREMIUM не может иметь > 5 подписок
    @Test
    void subscribe_ShouldThrowException_WhenPremiumLimitExceeded() {
        when(visualCrossingClient.getCurrentWeather("Kazan", TEST_LANG)).thenReturn(mockApiResponse);
        when(userRepository.findByApiKey(PREMIUM_KEY)).thenReturn(Optional.of(premiumUser));
        when(subscriptionRepository.existsByUserAndCity(premiumUser, "Kazan")).thenReturn(false);

        List<UserSubscription> existing = List.of(
                createSub("Moscow"), createSub("Sochi"), createSub("SPb"),
                createSub("Kazan2"), createSub("Novosibirsk")
        );
        when(subscriptionRepository.findByUser(premiumUser)).thenReturn(existing);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> subscriptionService.subscribe(PREMIUM_KEY, "Kazan", true, true, true, true));
        assertEquals("PREMIUM подписка позволяет иметь не более 5 подписок", ex.getMessage());
    }

    //Проверяет обновление существующей подписки
    @Test
    void subscribe_ShouldUpdateExisting_WhenCityAlreadySubscribed() {
        when(visualCrossingClient.getCurrentWeather(TEST_CITY, TEST_LANG)).thenReturn(mockApiResponse);
        when(userRepository.findByApiKey(BASIC_KEY)).thenReturn(Optional.of(basicUser));
        when(subscriptionRepository.existsByUserAndCity(basicUser, TEST_CITY)).thenReturn(true);

        UserSubscription existing = new UserSubscription();
        existing.setCity(TEST_CITY);
        existing.setNotifyHeat(false);
        when(subscriptionRepository.findByUser(basicUser)).thenReturn(List.of(existing));

        subscriptionService.subscribe(BASIC_KEY, TEST_CITY, true, false, true, false);

        assertTrue(existing.isNotifyHeat());
        assertFalse(existing.isNotifyCold());
        verify(subscriptionRepository, times(1)).save(existing);
    }

    //Проверяет успешную отписку по id
    @Test
    void unsubscribe_ShouldDeleteSubscription_WhenFound() {
        UserSubscription sub = createSub(TEST_CITY);
        when(userRepository.findByEmail(BASIC_EMAIL)).thenReturn(Optional.of(basicUser));
        when(subscriptionRepository.findByIdAndUser(1L, basicUser)).thenReturn(Optional.of(sub));

        subscriptionService.unsubscribe(1L, BASIC_EMAIL);

        verify(subscriptionRepository, times(1)).delete(sub);
    }

    //Проверяет: ошибка если подписка не найдена
    @Test
    void unsubscribe_ShouldThrowException_WhenNotFound() {
        when(userRepository.findByEmail(BASIC_EMAIL)).thenReturn(Optional.of(basicUser));
        when(subscriptionRepository.findByIdAndUser(999L, basicUser)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> subscriptionService.unsubscribe(999L, BASIC_EMAIL));
    }

    //Проверяет отписку от всех подписок
    @Test
    void unsubscribeAll_ShouldDeleteAllUserSubscriptions() {
        when(userRepository.findByEmail(BASIC_EMAIL)).thenReturn(Optional.of(basicUser));

        subscriptionService.unsubscribeAll(BASIC_EMAIL);

        verify(subscriptionRepository, times(1)).deleteByUser(basicUser);
    }

    //Проверяет получение всех подписок пользователя
    @Test
    void getUserSubscriptions_ShouldReturnList() {
        UserSubscription sub = createSub(TEST_CITY);
        when(userRepository.findByEmail(BASIC_EMAIL)).thenReturn(Optional.of(basicUser));
        when(subscriptionRepository.findByUser(basicUser)).thenReturn(List.of(sub));

        List<UserSubscription> result = subscriptionService.getUserSubscriptions(BASIC_EMAIL);

        assertEquals(1, result.size());
        assertEquals(TEST_CITY, result.getFirst().getCity());
    }

    //Проверяет получение всех подписок системы
    @Test
    void getAllSubscriptions_ShouldReturnAll() {
        when(subscriptionRepository.findAll()).thenReturn(List.of(createSub("Moscow"), createSub("Sochi")));

        List<UserSubscription> result = subscriptionService.getAllSubscriptions();

        assertEquals(2, result.size());
    }
}