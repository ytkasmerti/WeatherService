package ru.urfu.webapplication.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.service.PaymentService;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    //Информация о подписке
    //http://localhost:8080/payment/subscription/info?apiKey=free-1c6f8bdf-996408441
    //http://localhost:8080/payment/subscription/info?apiKey=premium-4fde29ef-1676466811
    @GetMapping("/subscription/info")
    public Map<String, Object> getSubscriptionInfo(@RequestParam String apiKey) {
        return paymentService.getSubscriptionInfo(apiKey);
    }
}