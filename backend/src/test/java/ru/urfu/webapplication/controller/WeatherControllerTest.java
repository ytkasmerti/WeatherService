package ru.urfu.webapplication.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.urfu.webapplication.dto.ForecastResponse;
import ru.urfu.webapplication.dto.HistoricalResponse;
import ru.urfu.webapplication.dto.HourlyForecastResponse;
import ru.urfu.webapplication.dto.WeatherResponse;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.security.ApiKeyAuthenticationFilter;
import ru.urfu.webapplication.security.CookieAuthenticationFilter;
import ru.urfu.webapplication.security.WeatherUserDetails;
import ru.urfu.webapplication.security.WeatherUserDetailsService;
import ru.urfu.webapplication.service.WeatherService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
@AutoConfigureMockMvc(addFilters = false)
class WeatherControllerTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_LANG = "ru";

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private WeatherService weatherService;
    @MockitoBean
    private WeatherUserDetailsService weatherUserDetailsService;
    @MockitoBean
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    @MockitoBean
    private CookieAuthenticationFilter cookieAuthenticationFilter;

    private WeatherUserDetails freeUser;
    private WeatherUserDetails basicUser;
    private WeatherUserDetails premiumUser;

    @BeforeEach
    void setUp() {
        freeUser = createUser(SubscriptionLevel.FREE);
        basicUser = createUser(SubscriptionLevel.BASIC);
        premiumUser = createUser(SubscriptionLevel.PREMIUM);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private WeatherUserDetails createUser(SubscriptionLevel level) {
        User u = new User();
        u.setId(1L);
        u.setEmail(TEST_EMAIL);
        u.setApiKey(level.name().toLowerCase() + "-key");
        u.setSubscriptionLevel(level);
        u.setIsActive(true);
        return new WeatherUserDetails(u);
    }

    private void setAuth(WeatherUserDetails details) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities())
        );
    }

    //Проверяет текущую погоду по городу
    @Test
    void getCurrentWeather_ShouldReturn200_ForFreeUser() throws Exception {
        setAuth(freeUser);
        WeatherResponse resp = WeatherResponse.builder().location("Moscow").temperature(20.0).build();
        when(weatherService.getCurrentWeatherByCity(eq("Moscow"), anyString(), eq(TEST_LANG))).thenReturn(resp);

        mockMvc.perform(get("/weather/current").param("city", "Moscow").param("lang", TEST_LANG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Moscow"));
    }

    // Проверяет текущую погоду по координатам
    @Test
    void getCurrentWeatherByCoordinates_ShouldReturn200_ForFreeUser() throws Exception {
        setAuth(freeUser);
        WeatherResponse resp = WeatherResponse.builder().location("55.75,37.61").build();
        when(weatherService.getCurrentWeatherByCoordinates(eq(55.75), eq(37.61), anyString(), eq(TEST_LANG)))
                .thenReturn(resp);

        mockMvc.perform(get("/weather/current")
                        .param("lat", "55.75")
                        .param("lon", "37.61")
                        .param("lang", TEST_LANG))
                .andExpect(status().isOk());
    }

    //Проверяет прогноз для BASIC
    @Test
    void getForecast_ShouldReturn200_ForBasicUser() throws Exception {
        setAuth(basicUser);
        ForecastResponse resp = ForecastResponse.builder().location("Moscow").daily(List.of()).build();
        when(weatherService.getForecast(eq("Moscow"), eq(7), anyString(), eq(TEST_LANG))).thenReturn(resp);

        mockMvc.perform(get("/weather/forecast").param("city", "Moscow").param("days", "7"))
                .andExpect(status().isOk());
    }

    //Проверяет исторические данные для BASIC
    @Test
    void getHistoricalData_ShouldReturn200_ForBasicUser() throws Exception {
        setAuth(basicUser);
        HistoricalResponse resp = HistoricalResponse.builder()
                .location("Moscow").startDate("2025-01-01").endDate("2025-01-05").data(List.of()).build();
        when(weatherService.getHistoricalData(eq("Moscow"), eq("2025-01-01"), eq("2025-01-05"), anyString(), eq(TEST_LANG)))
                .thenReturn(resp);

        mockMvc.perform(get("/weather/history")
                        .param("city", "Moscow")
                        .param("start", "2025-01-01")
                        .param("end", "2025-01-05"))
                .andExpect(status().isOk());
    }

    //Проверяет прогноз в конкретное время для PREMIUM
    @Test
    void getWeatherAtTime_ShouldReturn200_ForPremiumUser() throws Exception {
        setAuth(premiumUser);
        WeatherResponse resp = WeatherResponse.builder().location("Moscow").build();
        when(weatherService.getWeatherAtTime(eq("Moscow"), eq("2025-01-01T12:00:00"), anyString(), eq(TEST_LANG)))
                .thenReturn(resp);

        mockMvc.perform(get("/weather/current/time")
                        .param("city", "Moscow")
                        .param("datetime", "2025-01-01T12:00:00"))
                .andExpect(status().isOk());
    }

    //Проверяет почасовой прогноз для PREMIUM
    @Test
    void getHourlyForecast_ShouldReturn200_ForPremiumUser() throws Exception {
        setAuth(premiumUser);
        HourlyForecastResponse resp = HourlyForecastResponse.builder()
                .location("Moscow").date("2025-01-01").hours(List.of()).build();
        when(weatherService.getHourlyForecast(eq("Moscow"), eq("2025-01-01"), anyString(), eq(TEST_LANG)))
                .thenReturn(resp);

        mockMvc.perform(get("/weather/forecast/hourly")
                        .param("city", "Moscow")
                        .param("date", "2025-01-01"))
                .andExpect(status().isOk());
    }

    //Проверяет фильтрацию для PREMIUM
    @Test
    void getForecastWithFilter_ShouldReturn200_ForPremiumUser() throws Exception {
        setAuth(premiumUser);
        ForecastResponse resp = ForecastResponse.builder().location("Moscow").daily(List.of()).build();
        when(weatherService.getForecastWithFilter(eq("Moscow"), eq(7), anyString(), eq(TEST_LANG), eq("дождь")))
                .thenReturn(resp);

        mockMvc.perform(get("/weather/forecast/filter")
                        .param("city", "Moscow")
                        .param("days", "7")
                        .param("filterCondition", "дождь"))
                .andExpect(status().isOk());
    }

    //Проверяет пустой город
    @Test
    void getCurrentWeather_ShouldReturn400_WhenCityEmpty() throws Exception {
        setAuth(freeUser);

        mockMvc.perform(get("/weather/current").param("city", ""))
                .andExpect(status().isBadRequest());
    }

    //Проверяет дней > 90
    @Test
    void getForecast_ShouldReturn400_WhenDaysTooLarge() throws Exception {
        setAuth(basicUser);

        mockMvc.perform(get("/weather/forecast")
                        .param("city", "Moscow")
                        .param("days", "200"))
                .andExpect(status().isBadRequest());
    }
}