package ru.urfu.webapplication.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.service.SubscriptionService;
import ru.urfu.webapplication.service.ApiKeyService;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final ApiKeyService apiKeyService;

    //Подписаться (только PREMIUM)
    @PreAuthorize("hasRole('PREMIUM')")
    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String apiKey,
                            @RequestParam String city,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyHeat,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyCold,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyWind,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyPrecipitation) {
        apiKeyService.getSubscriptionLevel(apiKey);
        String email = apiKeyService.getEmailByApiKey(apiKey);

        //Создание подписки
        UserSubscription sub = new UserSubscription();
        sub.setEmail(email);
        sub.setCity(city);
        sub.setNotifyHeat(notifyHeat);
        sub.setNotifyCold(notifyCold);
        sub.setNotifyWind(notifyWind);
        sub.setNotifyPrecipitation(notifyPrecipitation);
        subscriptionService.subscribe(sub);

        return "Вы подписались на уведомления о погоде в городе " + city;
    }
}