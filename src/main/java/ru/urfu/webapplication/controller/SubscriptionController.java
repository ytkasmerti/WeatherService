package ru.urfu.webapplication.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.service.SubscriptionService;
import ru.urfu.webapplication.service.ApiKeyService;

import java.util.List;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final ApiKeyService apiKeyService;

    //Подписаться
    // curl -X POST "http://localhost:8080/subscription/subscribe?apiKey=premium-a38dab44-1468544522&city=Moscow&notifyWind=false"
    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String apiKey,
                            @RequestParam String city,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyHeat,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyCold,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyWind,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyPrecipitation) {
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

    //Отписаться от всех
    //curl -X DELETE "http://localhost:8080/subscription/unsubscribe?apiKey=premium-a38dab44-1468544522"
    @DeleteMapping("/unsubscribe")
    public String unsubscribe(@RequestParam String apiKey) {
        String email = apiKeyService.getEmailByApiKey(apiKey);
        subscriptionService.unsubscribeAll(email);
        return "Вы отписались от всех уведомлений";
    }

    //Получить все свои подписки
    // http://localhost:8080/subscription/settings?apiKey=premium-a38dab44-1468544522
    @GetMapping("/settings")
    public List<UserSubscription> getSettings(@RequestParam String apiKey) {
        String email = apiKeyService.getEmailByApiKey(apiKey);
        return subscriptionService.getUserSubscriptions(email);
    }

    //Отписаться от конкретной подписки по id (id из settings)
    //curl -X DELETE "http://localhost:8080/subscription/unsubscribe/1?apiKey=premium-a38dab44-1468544522"
    @DeleteMapping("/unsubscribe/{subscriptionId}")
    public String unsubscribeById(@RequestParam String apiKey,
                                  @PathVariable Long subscriptionId) {
        String email = apiKeyService.getEmailByApiKey(apiKey);
        subscriptionService.unsubscribe(subscriptionId, email);
        return "Вы отписались от уведомлений подписки " + subscriptionId;
    }
}