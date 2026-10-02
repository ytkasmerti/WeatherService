package ru.urfu.webapplication.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.urfu.webapplication.dto.PaymentDto;
import ru.urfu.webapplication.dto.PaymentHistoryDto;
import ru.urfu.webapplication.model.PaymentStatus;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.security.ApiKeyAuthenticationFilter;
import ru.urfu.webapplication.security.CookieAuthenticationFilter;
import ru.urfu.webapplication.security.WeatherUserDetailsService;
import ru.urfu.webapplication.service.ApiKeyService;
import ru.urfu.webapplication.service.PaymentService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PaymentService paymentService;
    @MockitoBean
    private ApiKeyService apiKeyService;
    @MockitoBean
    private WeatherUserDetailsService weatherUserDetailsService;
    @MockitoBean
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    @MockitoBean
    private CookieAuthenticationFilter cookieAuthenticationFilter;

    //Проверяет создание платежа
    @Test
    void createPayment_ShouldReturn200() throws Exception {
        PaymentDto dto = PaymentDto.builder()
                .paymentId("p-1").level(SubscriptionLevel.BASIC).amount(500)
                .status(PaymentStatus.PENDING).build();

        when(paymentService.createPayment("free-key", SubscriptionLevel.BASIC, false)).thenReturn(dto);

        mockMvc.perform(post("/payment/create")
                        .param("apiKey", "free-key")
                        .param("level", "BASIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("p-1"));
    }

    //Проверяет подтверждение платежа
    @Test
    void confirmPayment_ShouldReturn200() throws Exception {
        PaymentDto dto = PaymentDto.builder()
                .paymentId("p-1").status(PaymentStatus.CONFIRMED).build();

        when(paymentService.confirmPayment("p-1", "free-key")).thenReturn(dto);

        mockMvc.perform(post("/payment/confirm/p-1")
                        .param("apiKey", "free-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    //Проверяет статус платежа
    @Test
    void getPaymentStatus_ShouldReturn200() throws Exception {
        PaymentDto dto = PaymentDto.builder().paymentId("p-1").status(PaymentStatus.PENDING).build();

        when(paymentService.getPaymentStatus("p-1")).thenReturn(dto);

        mockMvc.perform(get("/payment/status/p-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("p-1"));
    }

    //Проверяет историю платежей
    @Test
    void getPaymentHistory_ShouldReturn200() throws Exception {
        PaymentHistoryDto dto = PaymentHistoryDto.builder()
                .payments(List.of()).totalCount(0).currentPage(0).pageSize(20).build();

        when(apiKeyService.getEmailByApiKey("free-key")).thenReturn("test@example.com");
        when(paymentService.getPaymentHistoryByEmail("test@example.com", 0, 20)).thenReturn(dto);

        mockMvc.perform(get("/payment/history")
                        .param("apiKey", "free-key")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    //Проверяет установку большого количества для вывода истории платежей
    @Test
    void getPaymentHistory_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/payment/history")
                        .param("apiKey", "free-key")
                        .param("size", "200"))
                .andExpect(status().isBadRequest());
    }
}