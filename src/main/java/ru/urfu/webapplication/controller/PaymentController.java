package ru.urfu.webapplication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.dto.PaymentDto;
import ru.urfu.webapplication.security.WeatherUserDetails;
import ru.urfu.webapplication.service.PaymentService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

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

    // Информация о подписке
    //http://localhost:8080/payment/subscription/info
    @GetMapping("/subscription/info")
    public Map<String, Object> getSubscriptionInfo(@RequestParam(required = false) String apiKey) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return paymentService.getSubscriptionInfo(validApiKey);
    }

    // Создание платежа
    //curl -X POST "http://localhost:8080/payment/create?level=PREMIUM" -b cookies.txt
    @PostMapping("/create")
    public PaymentDto createPayment(
            @RequestParam(required = false) String apiKey,
            @RequestParam String level) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return paymentService.createPayment(validApiKey, level);
    }

    // Подтверждение оплаты
    //curl -X POST "http://localhost:8080/payment/confirm/fb91e4ac-c539-44fa-b946-656290196ce0" -b cookies.txt
    @PostMapping("/confirm/{paymentId}")
    public PaymentDto confirmPayment(
            @PathVariable String paymentId,
            @RequestParam(required = false) String apiKey) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return paymentService.confirmPayment(paymentId, validApiKey);
    }
}