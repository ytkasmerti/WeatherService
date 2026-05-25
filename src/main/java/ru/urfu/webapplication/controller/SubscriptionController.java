package ru.urfu.webapplication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.service.WeatherSubscriptionService;
import ru.urfu.webapplication.service.ApiKeyService;
import ru.urfu.webapplication.service.UserService;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final WeatherSubscriptionService weatherSubscriptionService;
    private final ApiKeyService apiKeyService;
    private final UserService userService;

    //Подписаться
    // curl -X POST "http://localhost:8080/subscription/subscribe?apiKey=premium-a38dab44-1468544522&city=Moscow&notifyWind=false"

    //Invoke-WebRequest -Method POST -Uri "http://localhost:8080/subscription/subscribe?apiKey=premium-fb174fac-721323663&city=Moscow&notifyWind=false"
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
        weatherSubscriptionService.subscribe(sub);
        return "Вы подписались на уведомления о погоде в городе " + city;
    }

    //Отписаться от всех
    //curl -X DELETE "http://localhost:8080/subscription/unsubscribe?apiKey=premium-a38dab44-1468544522"

    //Invoke-WebRequest -Method POST -Uri "http://localhost:8080/subscription/unsubscribe?apiKey=premium-fb174fac-721323663"
    @DeleteMapping("/unsubscribe")
    public String unsubscribe(@RequestParam String apiKey) {
        String email = apiKeyService.getEmailByApiKey(apiKey);
        weatherSubscriptionService.unsubscribeAll(email);
        return "Вы отписались от всех уведомлений";
    }

    //Получить все свои подписки
    // http://localhost:8080/subscription/settings?apiKey=premium-a38dab44-1468544522

    //Invoke-WebRequest -Method GET -Uri "http://localhost:8080/subscription/settings?apiKey=premium-fb174fac-721323663"
    @GetMapping("/settings")
    public List<UserSubscription> getSettings(@RequestParam String apiKey) {
        String email = apiKeyService.getEmailByApiKey(apiKey);
        return weatherSubscriptionService.getUserSubscriptions(email);
    }

    //Отписаться от конкретной подписки по id (id из settings)
    //curl -X DELETE "http://localhost:8080/subscription/unsubscribe/1?apiKey=premium-a38dab44-1468544522"
    @DeleteMapping("/unsubscribe/{subscriptionId}")
    public String unsubscribeById(@RequestParam String apiKey,
                                  @PathVariable Long subscriptionId) {
        String email = apiKeyService.getEmailByApiKey(apiKey);
        weatherSubscriptionService.unsubscribe(subscriptionId, email);
        return "Вы отписались от уведомлений подписки " + subscriptionId;
    }

    @PutMapping("/auto-renewal")
    public Map<String, Object> setAutoRenewal(
            @RequestParam String apiKey,
            @RequestParam boolean enabled) {

        String email = apiKeyService.getEmailByApiKey(apiKey);
        User user = userService.findByEmail(email);
        user.setAutoRenewal(enabled);
        userService.updateUser(user);

        log.info("Пользователь {} {} автопродление подписки", email, enabled ? "включил" : "отключил");

        return Map.of(
                "success", true,
                "autoRenewal", enabled,
                "message", String.format("Автопродление %s", enabled ? "включено" : "отключено")
        );
    }
}