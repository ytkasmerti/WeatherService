package ru.urfu.webapplication.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.dto.*;
import ru.urfu.webapplication.security.WeatherUserDetails;
import ru.urfu.webapplication.service.UserService;
import ru.urfu.webapplication.service.WeatherService;

import java.time.LocalDate;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/weather")
public class WeatherController {
    private final WeatherService weatherService;
    private final UserService userService;

    public WeatherController(WeatherService weatherService, UserService userService) {
        this.weatherService = weatherService;
        this.userService = userService;
    }

    // Регистрация
    //curl -X POST "http://localhost:8080/weather/register?email=weatherservice26@gmail.com&password=123456&confirmPassword=123456"
    //далее вход с куки
    //curl -X POST "http://localhost:8080/auth/login?email=weatherservice26@gmail.com&password=123456" -c cookies.txt

    @PostMapping("/register")
    public Map<String, String> register(@RequestParam @NotBlank String email,
                                        @RequestParam @NotBlank
                                        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов") String password,
                                        @RequestParam @NotBlank String confirmPassword) {
        return userService.registerUser(email, password, confirmPassword);
    }

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

    // Текущая погода по городу
    //http://localhost:8080/weather/current?city=Moscow
    @GetMapping("/current")
    public WeatherResponse getCurrentWeather(@RequestParam @NotBlank String city,
                                             @RequestParam(required = false) String apiKey,
                                             @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getCurrentWeatherByCity(city, validApiKey, lang);
    }

    // Текущая погода по координатам
    //http://localhost:8080/weather/current?lat=59.93&lon=30.31
    //ошибка валидации
    //http://localhost:8080/weather/current?lat=100&lon=37.62
    //http://localhost:8080/weather/current?lat=55.75&lon=200
    @GetMapping(value = "/current", params = {"lat", "lon"})
    public WeatherResponse getCurrentWeatherByCoordinates(@RequestParam @Min(-90) @Max(90) Double lat,
                                                          @RequestParam @Min(-180) @Max(180) Double lon,
                                                          @RequestParam(required = false) String apiKey,
                                                          @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getCurrentWeatherByCoordinates(lat, lon, validApiKey, lang);
    }

    // Прогноз на N дней
    //доступ BASIC и PREMIUM
    //http://localhost:8080/weather/forecast?city=Moscow&days=3
    //прогноз на максимальное количество дней
    //http://localhost:8080/weather/forecast?city=Sochi&days=15
    //ошибка валидации
    //http://localhost:8080/weather/forecast?city=Moscow&days=0
    @GetMapping("/forecast")
    public ForecastResponse getForecast(@RequestParam @NotBlank String city,
                                        @RequestParam(defaultValue = "7") @Min(1) @Max(90) int days,
                                        @RequestParam(required = false) String apiKey,
                                        @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getForecast(city, days, validApiKey, lang);
    }

    // Исторические данные за период
    //доступ BASIC(максимум 7 дней назад и максимум 7 дней периода) и PREMIUM (до 8 месяцев)
    //http://localhost:8080/weather/history?city=Moscow&start=2026-05-23&end=2026-05-25
    //http://localhost:8080/weather/history?city=Sochi&start=2026-04-25&end=2026-05-25 - только PREMIUM
    @GetMapping("/history")
    public HistoricalResponse getHistoricalData(@RequestParam @NotBlank String city,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                                @RequestParam(required = false) String apiKey,
                                                @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getHistoricalData(city, start.toString(), end.toString(), validApiKey, lang);
    }

    // Исторические данные о погоде в конкретную дату (BASIC - максимум 7 дней назад, PREMIUM - до 5 лет назад)
    //http://localhost:8080/weather/history/date?city=Moscow&date=2026-05-25
    @GetMapping("/history/date")
    public HistoricalResponse getHistoricalDataByDate(@RequestParam @NotBlank String city,
                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                      @RequestParam(required = false) String apiKey,
                                                      @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getHistoricalData(city, date.toString(), date.toString(), validApiKey, lang);
    }

    // Погода в конкретное время
    //доступ только PREMIUM
    //http://localhost:8080/weather/current/time?city=Moscow&datetime=2026-05-25T08:00:00
    @GetMapping("/current/time")
    public WeatherResponse getWeatherAtTime(@RequestParam @NotBlank String city,
                                            @RequestParam String datetime,
                                            @RequestParam(required = false) String apiKey,
                                            @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getWeatherAtTime(city, datetime, validApiKey, lang);
    }

    // Почасовой прогноз погоды
    //доступ только PREMIUM
    //http://localhost:8080/weather/forecast/hourly?city=Moscow&date=2026-05-25
    @GetMapping("/forecast/hourly")
    public HourlyForecastResponse getHourlyForecast(@RequestParam @NotBlank String city,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                    @RequestParam(required = false) String apiKey,
                                                    @RequestParam(defaultValue = "ru") String lang) {
        String validApiKey = getApiKeyFromRequestOrAuth(apiKey);
        return weatherService.getHourlyForecast(city, date.toString(), validApiKey, lang);
    }

    // Фильтрация прогноза погоды по погодным условиям
    //доступ только PREMIUM
    //http://localhost:8080/weather/forecast/filter?city=Moscow&days=7&filterCondition=sun
    //http://localhost:8080/weather/forecast/filter?city=Moscow&days=14&filterCondition=cloud
    @GetMapping("/forecast/filter")
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