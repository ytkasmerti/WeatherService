package ru.urfu.webapplication.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.urfu.webapplication.security.ApiKeyAuthenticationFilter;
import ru.urfu.webapplication.security.CookieAuthenticationFilter;
import ru.urfu.webapplication.security.WeatherUserDetailsService;
import ru.urfu.webapplication.service.AuthService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private WeatherUserDetailsService weatherUserDetailsService;
    @MockitoBean
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    @MockitoBean
    private CookieAuthenticationFilter cookieAuthenticationFilter;

    //Проверяет успешную регистрацию
    @Test
    void register_ShouldReturn200() throws Exception {
        when(authService.registerUser("new@example.com", "password123", "password123"))
                .thenReturn(Map.of("apiKey", "free-new-key"));

        mockMvc.perform(post("/auth/register")
                        .param("email", "new@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKey").value("free-new-key"));
    }

    //Проверяет короткий пароль
    @Test
    void register_ShouldReturn400_WhenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("email", "new@example.com")
                        .param("password", "123")
                        .param("confirmPassword", "123"))
                .andExpect(status().isBadRequest());
    }

    //Проверяет успешный вход
    @Test
    void login_ShouldReturn200() throws Exception {
        when(authService.login(eq("test@example.com"), eq("password123"), any()))
                .thenReturn(Map.of("success", true));

        mockMvc.perform(post("/auth/login")
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andExpect(status().isOk());
    }

    //Проверяет logout
    @Test
    void logout_ShouldReturn200() throws Exception {
        when(authService.logout(any())).thenReturn(Map.of("message", "Вы вышли"));

        mockMvc.perform(get("/auth/logout"))
                .andExpect(status().isOk());
    }

    //Проверяет checkAuth
    @Test
    void checkAuth_ShouldReturn200() throws Exception {
        when(authService.checkAuth("key")).thenReturn(Map.of("authenticated", true));

        mockMvc.perform(get("/auth/check")
                        .cookie(new jakarta.servlet.http.Cookie("apiKey", "key")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    //Проверяет forgot-password
    @Test
    void forgotPassword_ShouldReturn200() throws Exception {
        when(authService.forgotPassword("test@example.com"))
                .thenReturn(Map.of("message", "Код отправлен"));

        mockMvc.perform(post("/auth/forgot-password")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk());
    }

    //Проверяет reset-password
    @Test
    void resetPassword_ShouldReturn200() throws Exception {
        when(authService.resetPassword("test@example.com", "123456", "newpass123"))
                .thenReturn(Map.of("success", "true"));

        mockMvc.perform(post("/auth/reset-password")
                        .param("email", "test@example.com")
                        .param("code", "123456")
                        .param("newPassword", "newpass123"))
                .andExpect(status().isOk());
    }

    //Проверяет reset-password с коротким паролем
    @Test
    void resetPassword_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/reset-password")
                        .param("email", "test@example.com")
                        .param("code", "123456")
                        .param("newPassword", "123"))
                .andExpect(status().isBadRequest());
    }
}