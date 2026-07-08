package ru.urfu.webapplication.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.urfu.webapplication.annotation.RequireBasicOrPremium;
import ru.urfu.webapplication.annotation.RequirePremium;
import ru.urfu.webapplication.dto.ForecastResponse;
import ru.urfu.webapplication.dto.HistoricalResponse;
import ru.urfu.webapplication.dto.HourlyForecastResponse;
import ru.urfu.webapplication.dto.WeatherResponse;
import ru.urfu.webapplication.security.WeatherUserDetails;
import ru.urfu.webapplication.service.WeatherService;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/weather")
public class WeatherController {
    private final WeatherService weatherService;

    private String getApiKeyFromRequestOrAuth(String apiKeyParam) {
        if (apiKeyParam != null && !apiKeyParam.isEmpty()) {
            return apiKeyParam;
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof WeatherUserDetails userDetails) {
            return userDetails.getApiKey();
        }

        throw new RuntimeException("API ключ не найден. Передайте apiKey в параметрах или войдите через /auth/login");
    }

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    //Текущая погода по городу
    @GetMapping("/current")
    public WeatherResponse getCurrentWeather(@RequestParam @NotBlank String city,
                                             @RequestParam(required = false) String apiKey,
                                             @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getCurrentWeatherByCity(city, validApiKey, lang);
    }

    //Текущая погода по координатам
    @GetMapping(value = "/current", params = {"lat", "lon"})
    public WeatherResponse getCurrentWeatherByCoordinates(@RequestParam @Min(-90) @Max(90) Double lat,
                                                          @RequestParam @Min(-180) @Max(180) Double lon,
                                                          @RequestParam(required = false) String apiKey,
                                                          @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getCurrentWeatherByCoordinates(lat, lon, validApiKey, lang);
    }

    //Прогноз на N дней (максимум до 15 дней)
    //Доступ BASIC и PREMIUM
    @GetMapping("/forecast")
    @RequireBasicOrPremium
    public ForecastResponse getForecast(@RequestParam @NotBlank String city,
                                        @RequestParam(defaultValue = "7") @Min(1) @Max(90) int days,
                                        @RequestParam(required = false) String apiKey,
                                        @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getForecast(city, days, validApiKey, lang);
    }

    //Исторические данные за период
    //Доступ BASIC(максимум 7 дней назад и максимум 7 дней периода) и PREMIUM (до 8 месяцев)
    @GetMapping("/history")
    @RequireBasicOrPremium
    public HistoricalResponse getHistoricalData(@RequestParam @NotBlank String city,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                                @RequestParam(required = false) String apiKey,
                                                @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getHistoricalData(city, start.toString(), end.toString(), validApiKey, lang);
    }

    //Исторические данные о погоде в конкретную дату (BASIC - максимум 7 дней назад, PREMIUM - до 5 лет назад)
    @GetMapping("/history/date")
    @RequireBasicOrPremium
    public HistoricalResponse getHistoricalDataByDate(@RequestParam @NotBlank String city,
                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                      @RequestParam(required = false) String apiKey,
                                                      @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getHistoricalData(city, date.toString(), date.toString(), validApiKey, lang);
    }

    //Погода в конкретное время
    //Доступ только PREMIUM
    @GetMapping("/current/time")
    @RequirePremium
    public WeatherResponse getWeatherAtTime(@RequestParam @NotBlank String city,
                                            @RequestParam String datetime,
                                            @RequestParam(required = false) String apiKey,
                                            @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getWeatherAtTime(city, datetime, validApiKey, lang);
    }

    //Почасовой прогноз погоды
    //Доступ только PREMIUM
    @GetMapping("/forecast/hourly")
    @RequirePremium
    public HourlyForecastResponse getHourlyForecast(@RequestParam @NotBlank String city,
                                                    @RequestParam String date,
                                                    @RequestParam(required = false) String apiKey,
                                                    @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getHourlyForecast(city, date, validApiKey, lang);
    }

    //Фильтрация прогноза погоды по погодным условиям
    //Доступ только PREMIUM
    @GetMapping("/forecast/filter")
    @RequirePremium
    public ForecastResponse getForecastWithFilter(
            @RequestParam @NotBlank String city,
            @RequestParam(defaultValue = "7") @Min(1) @Max(90) int days,
            @RequestParam(defaultValue = "ru") String lang,
            @RequestParam(required = false) String filterCondition,
            @RequestParam(required = false) String apiKey) {

        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getForecastWithFilter(city, days, validApiKey, lang, filterCondition);
    }
}