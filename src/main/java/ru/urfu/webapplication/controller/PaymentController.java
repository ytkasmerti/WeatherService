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
    @GetMapping("/subscription/info")
    public Map<String, Object> getSubscriptionInfo(@RequestParam String apiKey) {
        return paymentService.getSubscriptionInfo(apiKey);
    }

    //Создание платежа
    //в cmd:  curl -X POST "http://localhost:8080/payment/create?apiKey= ApiKey &plan=BASIC"
    @PostMapping("/create")
    public PaymentDto createPayment(
            @RequestParam String apiKey,
            @RequestParam String plan) {
        return paymentService.createPayment(apiKey, plan);
    }

    //Мок-подтверждение оплаты
    //в cmd: curl -X POST "http://localhost:8080/payment/confirm/ PaymentId ?apiKey= ApiKey "
    @PostMapping("/confirm/{paymentId}")
    public PaymentDto confirmPayment(
            @PathVariable String paymentId,
            @RequestParam String apiKey) {
        return paymentService.confirmPayment(paymentId, apiKey);
    }
}