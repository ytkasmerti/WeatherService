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
import ru.urfu.webapplication.dto.WeatherSubscriptionDto;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.security.ApiKeyAuthenticationFilter;
import ru.urfu.webapplication.security.CookieAuthenticationFilter;
import ru.urfu.webapplication.security.WeatherUserDetails;
import ru.urfu.webapplication.security.WeatherUserDetailsService;
import ru.urfu.webapplication.service.ApiKeyService;
import ru.urfu.webapplication.service.DtoMapperService;
import ru.urfu.webapplication.service.WeatherSubscriptionService;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private WeatherSubscriptionService weatherSubscriptionService;
    @MockitoBean
    private ApiKeyService apiKeyService;
    @MockitoBean
    private DtoMapperService dtoMapperService;
    @MockitoBean
    private WeatherUserDetailsService weatherUserDetailsService;
    @MockitoBean
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    @MockitoBean
    private CookieAuthenticationFilter cookieAuthenticationFilter;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setApiKey("basic-key");
        testUser.setSubscriptionLevel(SubscriptionLevel.BASIC);
        testUser.setIsActive(true);

        WeatherUserDetails userDetails = new WeatherUserDetails(testUser);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    //Проверяет успешную подписку
    @Test
    void subscribe_ShouldReturn200() throws Exception {
        doNothing().when(weatherSubscriptionService)
                .subscribe(eq("basic-key"), eq("Moscow"), eq(true), eq(true), eq(true), eq(true));

        mockMvc.perform(get("/subscription/subscribe").param("city", "Moscow"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Moscow")));
    }

    //Проверяет пустой город при подписке
    @Test
    void subscribe_ShouldReturn400_WhenCityEmpty() throws Exception {
        mockMvc.perform(get("/subscription/subscribe").param("city", ""))
                .andExpect(status().isBadRequest());
    }

    //Проверяет список подписок
    @Test
    void getSettings_ShouldReturn200() throws Exception {
        UserSubscription sub = new UserSubscription();
        sub.setId(1L);
        sub.setCity("Moscow");

        WeatherSubscriptionDto dto = WeatherSubscriptionDto.builder()
                .id(1L).city("Moscow").build();

        when(apiKeyService.getEmailByApiKey("basic-key")).thenReturn("test@example.com");
        when(weatherSubscriptionService.getUserSubscriptions("test@example.com"))
                .thenReturn(List.of(sub));
        when(dtoMapperService.toWeatherSubscriptionDto(sub)).thenReturn(dto);

        mockMvc.perform(get("/subscription/subscribtions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Moscow"));
    }

    //Проверяет отписку от всех подписок
    @Test
    void unsubscribe_ShouldReturn200() throws Exception {
        when(apiKeyService.getEmailByApiKey("basic-key")).thenReturn("test@example.com");
        doNothing().when(weatherSubscriptionService).unsubscribeAll("test@example.com");

        mockMvc.perform(get("/subscription/unsubscribe"))
                .andExpect(status().isOk());
    }

    //Проверяет отписку по id
    @Test
    void unsubscribeById_ShouldReturn200() throws Exception {
        when(apiKeyService.getEmailByApiKey("basic-key")).thenReturn("test@example.com");
        doNothing().when(weatherSubscriptionService).unsubscribe(1L, "test@example.com");

        mockMvc.perform(get("/subscription/unsubscribe/1"))
                .andExpect(status().isOk());
    }
}