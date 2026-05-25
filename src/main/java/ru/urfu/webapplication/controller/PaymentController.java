package ru.urfu.webapplication.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.dto.PaymentDto;
import ru.urfu.webapplication.service.PaymentService;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    //Информация о подписке
    //http://localhost:8080/payment/subscription/info?apiKey=free-5064c80f-130987652
    //http://localhost:8080/payment/subscription/info?apiKey=premium-4fde29ef-1676466811

    //http://localhost:8080/payment/subscription/info?apiKey=basic-dd0e6290-721323663
    @GetMapping("/subscription/info")
    public Map<String, Object> getSubscriptionInfo(@RequestParam String apiKey) {
        return paymentService.getSubscriptionInfo(apiKey);
    }

    //Создание платежа
    //curl -X POST "http://localhost:8080/payment/create?apiKey= ApiKey &level=BASIC"

    //Invoke-WebRequest -Method POST -Uri "http://localhost:8080/payment/create?apiKey=basic-dd0e6290-721323663&level=PREMIUM"
    @PostMapping("/create")
    public PaymentDto createPayment(
            @RequestParam String apiKey,
            @RequestParam String level) {
        return paymentService.createPayment(apiKey, level);
    }

    //Имитация оплаты
    //curl -X POST "http://localhost:8080/payment/confirm/ PaymentId ?apiKey= ApiKey "

    //Invoke-WebRequest -Method POST -Uri "http://localhost:8080/payment/confirm/93520477-0a7e-4cec-8dc1-5eaec570d3f4?apiKey=basic-dd0e6290-721323663"
    @PostMapping("/confirm/{paymentId}")
    public PaymentDto confirmPayment(
            @PathVariable String paymentId,
            @RequestParam String apiKey) {
        return paymentService.confirmPayment(paymentId, apiKey);
    }
}