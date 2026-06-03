package ru.urfu.webapplication.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.dto.PaymentDto;
import ru.urfu.webapplication.dto.PaymentHistoryDto;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.security.WeatherUserDetails;
import ru.urfu.webapplication.service.ApiKeyService;
import ru.urfu.webapplication.service.PaymentService;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final ApiKeyService apiKeyService;

    private String getApiKeyFromRequestOrAuth(String apiKeyParam) {
        if (apiKeyParam != null && !apiKeyParam.isEmpty()) {
            return apiKeyParam;
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof WeatherUserDetails userDetails) {
            return userDetails.getApiKey();
        }

        throw new RuntimeException("API ключ не найден");
    }

    // Создание платежа
    @PostMapping("/create")
    public PaymentDto createPayment(
            @RequestParam(required = false) String apiKey,
            @RequestParam SubscriptionLevel level) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return paymentService.createPayment(validApiKey, level, false);
    }

    // Подтверждение оплаты
    @PostMapping("/confirm/{paymentId}")
    public PaymentDto confirmPayment(
            @PathVariable String paymentId,
            @RequestParam(required = false) String apiKey) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return paymentService.confirmPayment(paymentId, validApiKey);
    }

    //Проверка статуса платежа
    @GetMapping("/status/{paymentId}")
    public PaymentDto getPaymentStatus(@PathVariable String paymentId) {
        return paymentService.getPaymentStatus(paymentId);
    }

    //Получить историю платежей
    @GetMapping("/history")
    public PaymentHistoryDto getPaymentHistory(@RequestParam(required = false) String apiKey,
                                               @RequestParam (defaultValue = "0") @Min(0) int page,
                                               @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        String email = apiKeyService.getEmailByApiKey(validApiKey);
        return paymentService.getPaymentHistoryByEmail(email, page, size);
    }
}