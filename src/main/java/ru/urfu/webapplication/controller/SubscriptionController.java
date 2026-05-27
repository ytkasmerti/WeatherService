package ru.urfu.webapplication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.security.WeatherUserDetails;
import ru.urfu.webapplication.service.WeatherSubscriptionService;
import ru.urfu.webapplication.service.ApiKeyService;
import ru.urfu.webapplication.service.UserService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final WeatherSubscriptionService weatherSubscriptionService;
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

    private String getEmailFromApiKey(String apiKey) {
        return apiKeyService.getEmailByApiKey(apiKey);
    }

    //Подписаться
    // http://localhost:8080/subscription/subscribe?city=Moscow&notifyWind=false
    // http://localhost:8080/subscription/subscribe?city=Moscow
    @GetMapping("/subscribe")
    public String subscribe(@RequestParam(required = false) String apiKey,
                            @RequestParam String city,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyHeat,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyCold,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyWind,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyPrecipitation) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        String email = getEmailFromApiKey(validApiKey);

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
    // http://localhost:8080/subscription/unsubscribe
    @GetMapping("/unsubscribe")
    public String unsubscribe(@RequestParam(required = false) String apiKey) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        String email = getEmailFromApiKey(validApiKey);
        weatherSubscriptionService.unsubscribeAll(email);
        return "Вы отписались от всех уведомлений";
    }

    //Получить все подписки
    // http://localhost:8080/subscription/subscribtions
    @GetMapping("/subscribtions")
    public List<UserSubscription> getSettings(@RequestParam(required = false) String apiKey) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        String email = getEmailFromApiKey(validApiKey);
        return weatherSubscriptionService.getUserSubscriptions(email);
    }

    //Отписаться по конкретному id
    // http://localhost:8080/subscription/unsubscribe/3
    @GetMapping("/unsubscribe/{subscriptionId}")
    public String unsubscribeById(@RequestParam(required = false) String apiKey,
                                  @PathVariable Long subscriptionId) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        String email = getEmailFromApiKey(validApiKey);
        weatherSubscriptionService.unsubscribe(subscriptionId, email);
        return "Вы отписались от уведомлений подписки " + subscriptionId;
    }
}