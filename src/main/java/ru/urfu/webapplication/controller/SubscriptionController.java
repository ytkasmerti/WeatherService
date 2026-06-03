package ru.urfu.webapplication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.dto.WeatherSubscriptionDto;
import ru.urfu.webapplication.entity.UserSubscription;
import ru.urfu.webapplication.security.WeatherUserDetails;
import ru.urfu.webapplication.service.ApiKeyService;
import ru.urfu.webapplication.service.DtoMapperService;
import ru.urfu.webapplication.service.WeatherSubscriptionService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final WeatherSubscriptionService weatherSubscriptionService;
    private final ApiKeyService apiKeyService;
    private final DtoMapperService dtoMapperService;

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
    @GetMapping("/subscribe")
    public String subscribe(@RequestParam(required = false) String apiKey,
                            @RequestParam String city,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyHeat,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyCold,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyWind,
                            @RequestParam(required = false, defaultValue = "true") boolean notifyPrecipitation) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        weatherSubscriptionService.subscribe(validApiKey, city, notifyHeat, notifyCold, notifyWind, notifyPrecipitation);
        return "Вы подписались на уведомления о погоде в городе " + city;
    }

    //Отписаться от всех
    @GetMapping("/unsubscribe")
    public String unsubscribe(@RequestParam(required = false) String apiKey) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        String email = getEmailFromApiKey(validApiKey);
        weatherSubscriptionService.unsubscribeAll(email);
        return "Вы отписались от всех уведомлений";
    }

    //Получить все подписки
    @GetMapping("/subscribtions")
    public List<WeatherSubscriptionDto> getSettings(@RequestParam(required = false) String apiKey) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        String email = getEmailFromApiKey(validApiKey);
        List<UserSubscription> subscriptions = weatherSubscriptionService.getUserSubscriptions(email);
        return subscriptions.stream()
                .map(dtoMapperService::toWeatherSubscriptionDto)
                .toList();
    }

    //Отписаться по конкретному id
    @GetMapping("/unsubscribe/{subscriptionId}")
    public String unsubscribeById(@RequestParam(required = false) String apiKey,
                                  @PathVariable Long subscriptionId) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        String email = getEmailFromApiKey(validApiKey);
        weatherSubscriptionService.unsubscribe(subscriptionId, email);
        return "Вы отписались от уведомлений подписки " + subscriptionId;
    }
}