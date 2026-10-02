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
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.UserRepository;
import ru.urfu.webapplication.security.ApiKeyAuthenticationFilter;
import ru.urfu.webapplication.security.CookieAuthenticationFilter;
import ru.urfu.webapplication.security.WeatherUserDetails;
import ru.urfu.webapplication.security.WeatherUserDetailsService;
import ru.urfu.webapplication.service.UserService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private WeatherUserDetailsService weatherUserDetailsService;
    @MockitoBean
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    @MockitoBean
    private CookieAuthenticationFilter cookieAuthenticationFilter;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setApiKey("free-key");
        testUser.setSubscriptionLevel(SubscriptionLevel.FREE);
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

    //Проверяет вывод информации профиля
    @Test
    void getProfile_ShouldReturn200() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userService.getProfile("test@example.com"))
                .thenReturn(Map.of("email", "test@example.com"));

        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    //Проверяет ошибку при выводе информации профиля без аутентификации
    @Test
    void getProfile_ShouldFail_WhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/user/profile"))
                .andExpect(status().is4xxClientError());
    }

    //Проверяет автопродление подписки
    @Test
    void setAutoRenewal_ShouldReturn200() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userService.setAutoRenewal("test@example.com", true))
                .thenReturn(Map.of("success", true, "autoRenewal", true));

        mockMvc.perform(get("/user/auto-renewal").param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoRenewal").value(true));
    }

    //Проверяет удаление аккаунта
    @Test
    void deleteAccount_ShouldReturn200() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userService.deleteAccount(eq("test@example.com"), eq("password123"), any()))
                .thenReturn(Map.of("success", "true"));

        mockMvc.perform(delete("/user/delete").param("password", "password123"))
                .andExpect(status().isOk());
    }

    //Проверяет пустой пароль
    @Test
    void deleteAccount_ShouldReturn400() throws Exception {
        mockMvc.perform(delete("/user/delete").param("password", ""))
                .andExpect(status().isBadRequest());
    }

    //Проверяет смену пароля
    @Test
    void changePassword_ShouldReturn200() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userService.changePassword("test@example.com", "oldpass", "newpass123"))
                .thenReturn(Map.of("success", "true"));

        mockMvc.perform(post("/user/change-password")
                        .param("oldPassword", "oldpass")
                        .param("newPassword", "newpass123"))
                .andExpect(status().isOk());
    }

    //Проверяет короткий новый пароль
    @Test
    void changePassword_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/user/change-password")
                        .param("oldPassword", "oldpass")
                        .param("newPassword", "123"))
                .andExpect(status().isBadRequest());
    }
}