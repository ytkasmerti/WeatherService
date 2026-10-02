package ru.urfu.webapplication.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.security.WeatherUserDetails;
import ru.urfu.webapplication.service.WeatherService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WeatherControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;
    @MockitoBean
    private JavaMailSender javaMailSender;
    @MockitoBean
    private RedisConnectionFactory redisConnectionFactory;
    @MockitoBean
    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    private WeatherUserDetails createUser(SubscriptionLevel level) {
        User u = new User();
        u.setId(1L);
        u.setEmail("test@example.com");
        u.setApiKey(level.name().toLowerCase() + "-key");
        u.setSubscriptionLevel(level);
        u.setIsActive(true);
        return new WeatherUserDetails(u);
    }

    //Проверяет недоступность прогноза для FREE
    @Test
    void getForecast_ShouldReturn403_ForFreeUser() throws Exception {
        mockMvc.perform(get("/weather/forecast")
                        .param("city", "Moscow")
                        .param("days", "7")
                        .with(user(createUser(SubscriptionLevel.FREE))))
                .andExpect(status().isForbidden());
    }

    //Проверяет недоступность исторических данных для FREE
    @Test
    void getHistory_ShouldReturn403_ForFreeUser() throws Exception {
        mockMvc.perform(get("/weather/history")
                        .param("city", "Moscow")
                        .param("start", "2025-01-01")
                        .param("end", "2025-01-05")
                        .with(user(createUser(SubscriptionLevel.FREE))))
                .andExpect(status().isForbidden());
    }

    //Проверяет недоступность прогноза на конкретное время для BASIC
    @Test
    void getWeatherAtTime_ShouldReturn403_ForBasicUser() throws Exception {
        mockMvc.perform(get("/weather/current/time")
                        .param("city", "Moscow")
                        .param("datetime", "2025-01-01T12:00:00")
                        .with(user(createUser(SubscriptionLevel.BASIC))))
                .andExpect(status().isForbidden());
    }

    //Проверяет недоступность почасового прогноза для BASIC
    @Test
    void getHourlyForecast_ShouldReturn403_ForBasicUser() throws Exception {
        mockMvc.perform(get("/weather/forecast/hourly")
                        .param("city", "Moscow")
                        .param("date", "2025-01-01")
                        .with(user(createUser(SubscriptionLevel.BASIC))))
                .andExpect(status().isForbidden());
    }

    //Проверяет недоступность фильтрации прогноза для BASIC
    @Test
    void getForecastWithFilter_ShouldReturn403_ForBasicUser() throws Exception {
        mockMvc.perform(get("/weather/forecast/filter")
                        .param("city", "Moscow")
                        .param("days", "7")
                        .with(user(createUser(SubscriptionLevel.BASIC))))
                .andExpect(status().isForbidden());
    }
}