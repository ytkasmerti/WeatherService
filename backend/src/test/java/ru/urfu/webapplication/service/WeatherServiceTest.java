package ru.urfu.webapplication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.urfu.webapplication.client.VisualCrossingClient;
import ru.urfu.webapplication.dto.ForecastResponse;
import ru.urfu.webapplication.dto.WeatherResponse;
import ru.urfu.webapplication.dto.visualcrossingapi.CurrentConditions;
import ru.urfu.webapplication.dto.visualcrossingapi.Day;
import ru.urfu.webapplication.dto.visualcrossingapi.VisualCrossingResponse;
import ru.urfu.webapplication.entity.WeatherRequest;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.WeatherRequestRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    private static final String TEST_CITY = "Moscow";
    private static final String TEST_KEY = "free-test-key";
    private static final String TEST_LANG = "ru";

    @Mock
    private VisualCrossingClient visualCrossingClient;
    @Mock
    private ApiKeyService apiKeyService;
    @Mock
    private DtoMapperService mapper;
    @Mock
    private WeatherRequestRepository weatherRequestRepository;
    @Mock
    private WeatherAlertService weatherAlertService;

    @InjectMocks
    private WeatherService weatherService;

    private VisualCrossingResponse mockApiResponse;
    private WeatherResponse mockWeatherResponse;

    @BeforeEach
    void setUp() {
        mockApiResponse = new VisualCrossingResponse();

        CurrentConditions current = new CurrentConditions();
        current.setTemp(20.0);
        current.setFeelsLike(19.0);
        current.setHumidity(60.0);
        current.setWindSpeed(5.0);
        current.setWindDirection(180.0);
        current.setPressure(1013.0);
        current.setConditions("Clear");
        mockApiResponse.setCurrentConditions(current);
        mockApiResponse.setResolvedAddress(TEST_CITY);

        Day day1 = new Day();
        day1.setDatetime("2025-01-01");
        day1.setConditions("Rain");
        day1.setTempMax(25.0);
        day1.setTempMin(15.0);
        day1.setWindSpeed(5.0);

        Day day2 = new Day();
        day2.setDatetime("2025-01-02");
        day2.setConditions("Clear");

        mockApiResponse.setDays(List.of(day1, day2));

        mockWeatherResponse = WeatherResponse.builder()
                .location(TEST_CITY)
                .temperature(20.0)
                .feelsLike(19.0)
                .humidity(60)
                .windSpeed(5.0)
                .windDirection(180.0)
                .pressure(1013.0)
                .conditions("Clear")
                .build();
        ReflectionTestUtils.setField(weatherService, "self", weatherService);
    }

    //Проверяет успешное получение текущей погоды по городу для FREE
    @Test
    void getCurrentWeatherByCity_ShouldReturnWeather_WhenApiSucceeds() {
        when(apiKeyService.validateAndGetLevel(TEST_KEY)).thenReturn(SubscriptionLevel.FREE);
        when(visualCrossingClient.getCurrentWeather(TEST_CITY, TEST_LANG)).thenReturn(mockApiResponse);
        when(mapper.toWeatherResponse(mockApiResponse, TEST_CITY, TEST_LANG)).thenReturn(mockWeatherResponse);

        WeatherResponse result = weatherService.getCurrentWeatherByCity(TEST_CITY, TEST_KEY, TEST_LANG);

        assertNotNull(result);
        assertEquals(TEST_CITY, result.getLocation());
        assertEquals(20.0, result.getTemperature());
        verify(weatherRequestRepository, times(1)).save(any(WeatherRequest.class));
        verify(apiKeyService, times(1)).validateAndGetLevel(TEST_KEY);
    }

    //Проверяет ошибка при невалидном API ключе
    @Test
    void getCurrentWeatherByCity_ShouldThrowException_WhenApiKeyInvalid() {
        String apiKey = "invalid-key";
        when(apiKeyService.validateAndGetLevel(apiKey))
                .thenThrow(new RuntimeException("Неверный API ключ"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> weatherService.getCurrentWeatherByCity(TEST_CITY, apiKey, TEST_LANG));
        assertEquals("Неверный API ключ", ex.getMessage());
    }

    //Проверяет получение погоды по координатам для FREE
    @Test
    void getCurrentWeatherByCoordinates_ShouldReturnWeather_WhenApiSucceeds() {
        Double lat = 55.7558;
        Double lon = 37.6173;
        String location = lat + "," + lon;

        when(apiKeyService.validateAndGetLevel(TEST_KEY)).thenReturn(SubscriptionLevel.FREE);
        when(visualCrossingClient.getCurrentWeather(location, TEST_LANG)).thenReturn(mockApiResponse);
        when(mapper.toWeatherResponse(mockApiResponse, location, TEST_LANG)).thenReturn(mockWeatherResponse);

        WeatherResponse result = weatherService.getCurrentWeatherByCoordinates(lat, lon, TEST_KEY, TEST_LANG);

        assertNotNull(result);
        verify(visualCrossingClient, times(1)).getCurrentWeather(location, TEST_LANG);
        verify(weatherRequestRepository, times(1)).save(any(WeatherRequest.class));
    }

    //Проверяет прогноз для BASIC
    @Test
    void getForecast_ShouldReturnForecast_WhenBasicUser() {
        int days = 5;
        String apiKey = "basic-test-key";

        ForecastResponse.DailyForecast daily = ForecastResponse.DailyForecast.builder()
                .date("2025-01-01").tempMax(25.0).build();
        ForecastResponse expected = ForecastResponse.builder()
                .location(TEST_CITY).daily(List.of(daily)).build();

        when(apiKeyService.validateAndGetLevel(apiKey)).thenReturn(SubscriptionLevel.BASIC);
        when(visualCrossingClient.getForecast(TEST_CITY, days, TEST_LANG)).thenReturn(mockApiResponse);
        when(mapper.toDailyForecast(any(Day.class))).thenReturn(daily);
        when(mapper.toForecastResponse(eq(mockApiResponse), anyList())).thenReturn(expected);

        ForecastResponse result = weatherService.getForecast(TEST_CITY, days, apiKey, TEST_LANG);

        assertNotNull(result);
        assertEquals(TEST_CITY, result.getLocation());
    }

    //Проверяет ограничение прогноза 15 днями
    @Test
    void getForecast_ShouldLimitDaysTo15_WhenMoreRequested() {
        int days = 30;
        String apiKey = "basic-test-key";

        when(apiKeyService.validateAndGetLevel(apiKey)).thenReturn(SubscriptionLevel.BASIC);
        when(visualCrossingClient.getForecast(TEST_CITY, 15, TEST_LANG)).thenReturn(mockApiResponse);
        when(mapper.toForecastResponse(any(), anyList())).thenReturn(ForecastResponse.builder().build());

        weatherService.getForecast(TEST_CITY, days, apiKey, TEST_LANG);

        verify(visualCrossingClient, times(1)).getForecast(TEST_CITY, 15, TEST_LANG);
    }

    //Проверяет: BASIC не может запросить период > 7 дней
    @Test
    void getHistoricalData_ShouldThrowException_WhenBasicUserRequestsMoreThan7Days() {
        String apiKey = "basic-test-key";
        when(apiKeyService.validateAndGetLevel(apiKey)).thenReturn(SubscriptionLevel.BASIC);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> weatherService.getHistoricalData(TEST_CITY, "2025-01-01", "2025-01-15", apiKey, TEST_LANG));
        assertTrue(ex.getMessage().contains("не более чем на 7 дней"));
    }

    //Проверяет: BASIC не может запросить дату старше 7 дней
    @Test
    void getHistoricalData_ShouldThrowException_WhenBasicUserRequestsOlderThan7Days() {
        String apiKey = "basic-test-key";
        when(apiKeyService.validateAndGetLevel(apiKey)).thenReturn(SubscriptionLevel.BASIC);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> weatherService.getHistoricalData(TEST_CITY, "2020-01-01", "2020-01-05", apiKey, TEST_LANG));
        assertTrue(ex.getMessage().contains("не ранее чем 7 дней назад"));
    }

    //Проверяет: нельзя запрашивать будущие даты
    @Test
    void getHistoricalData_ShouldThrowException_WhenFutureDateRequested() {
        String apiKey = "premium-test-key";
        when(apiKeyService.validateAndGetLevel(apiKey)).thenReturn(SubscriptionLevel.PREMIUM);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> weatherService.getHistoricalData(TEST_CITY, "2030-01-01", "2030-01-05", apiKey, TEST_LANG));
        assertTrue(ex.getMessage().contains("будущих дат"));
    }

    //Проверяет фильтрацию прогноза для PREMIUM
    @Test
    void getForecastWithFilter_ShouldReturnFilteredForecast_WhenFilterMatches() {
        int days = 7;
        String apiKey = "premium-test-key";
        String filterCondition = "дождь";

        ForecastResponse.DailyForecast rainyDay = ForecastResponse.DailyForecast.builder()
                .date("2025-01-01").conditions("Rain").build();
        ForecastResponse.DailyForecast clearDay = ForecastResponse.DailyForecast.builder()
                .date("2025-01-02").conditions("Clear").build();

        ForecastResponse fullForecast = ForecastResponse.builder()
                .location(TEST_CITY).daily(List.of(rainyDay, clearDay)).build();

        when(apiKeyService.validateAndGetLevel(apiKey)).thenReturn(SubscriptionLevel.PREMIUM);
        when(visualCrossingClient.getForecast(TEST_CITY, days, TEST_LANG)).thenReturn(mockApiResponse);
        when(mapper.toDailyForecast(any()))
                .thenReturn(rainyDay)
                .thenReturn(clearDay);
        when(mapper.toForecastResponse(eq(mockApiResponse), anyList())).thenReturn(fullForecast);

        ForecastResponse result = weatherService.getForecastWithFilter(TEST_CITY, days, apiKey, TEST_LANG, filterCondition);

        assertNotNull(result);
        assertEquals(1, result.getDaily().size());
        assertEquals("Rain", result.getDaily().get(0).getConditions());
    }

    //Проверяет алерт при превышении порога жары
    @Test
    void checkWeatherConditions_ShouldReturnAlert_WhenHeatThresholdExceeded() {
        int days = 1;

        ReflectionTestUtils.setField(weatherService, "apiKey", "system-test-key");

        ForecastResponse.DailyForecast hotDay = ForecastResponse.DailyForecast.builder()
                .tempMax(35.0).tempMin(20.0).windSpeed(5.0).conditions("Clear").build();
        ForecastResponse forecast = ForecastResponse.builder()
                .location(TEST_CITY).daily(List.of(hotDay)).build();

        when(apiKeyService.validateAndGetLevel("system-test-key")).thenReturn(SubscriptionLevel.PREMIUM);
        when(visualCrossingClient.getForecast(TEST_CITY, days, TEST_LANG)).thenReturn(mockApiResponse);
        when(mapper.toDailyForecast(any())).thenReturn(hotDay);
        when(mapper.toForecastResponse(eq(mockApiResponse), anyList())).thenReturn(forecast);
        when(weatherAlertService.checkAlert(35.0, 20.0, 5.0, "Clear"))
                .thenReturn("Жара: 35.0 градусов");

        String alert = weatherService.checkWeatherConditions(TEST_CITY, days, TEST_LANG);

        assertNotNull(alert);
        assertTrue(alert.contains("Жара"));
    }
}