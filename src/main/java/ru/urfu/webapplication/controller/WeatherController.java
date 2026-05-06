package ru.urfu.webapplication.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.dto.ForecastResponse;
import ru.urfu.webapplication.dto.HistoricalResponse;
import ru.urfu.webapplication.dto.HourlyForecastResponse;
import ru.urfu.webapplication.dto.WeatherResponse;
import ru.urfu.webapplication.service.ApiKeyService;
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

    public WeatherController(WeatherService weatherService, ApiKeyService apiKeyService, UserService userService) {
        this.weatherService = weatherService;
        this.userService = userService;
    }

    //Регистрация
    // http://localhost:8080/weather/register?email=test4@mail.ru&plan=free      free-5064c80f-130987652
    // http://localhost:8080/weather/register?email=test2@mail.ru&plan=basic     basic-f27c60ec-811046022
    // http://localhost:8080/weather/register?email=test3@mail.ru&plan=premium   premium-4fde29ef-1676466811
    @GetMapping("/register")
    public Map<String, String> register(@RequestParam String email, @RequestParam(defaultValue = "free") String plan) {
        return userService.registerUser(email, plan);
    }

    //Текущая погода по городу
    // http://localhost:8080/weather/current?city=Moscow&apiKey=basic-f27c60ec-811046022
    // Пустой город - http://localhost:8080/weather/current?city=&apiKey=basic-f27c60ec-811046022
    @GetMapping("/current")
    public WeatherResponse getCurrentWeather(@RequestParam @NotBlank String city,
                                             @RequestParam String apiKey,
                                             @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getCurrentWeatherByCity(city, apiKey, lang);
    }

    //Текущая погода по координатам
    // http://localhost:8080/weather/current?lat=55.75&lon=37.62&apiKey=free-5064c80f-130987652&lang=en
    // некорректная широта http://localhost:8080/weather/current?lat=100&lon=37.62&apiKey=basic-f27c60ec-811046022
    // некорректная долгота http://localhost:8080/weather/current?lat=55.75&lon=200&apiKey=basic-f27c60ec-811046022
    @GetMapping(value = "/current", params = {"lat", "lon"})
    public WeatherResponse getCurrentWeatherByCoordinates(@RequestParam @Min(-90) @Max(90) Double lat,
                                                          @RequestParam @Min(-180) @Max(180) Double lon,
                                                          @RequestParam String apiKey,
                                                          @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getCurrentWeatherByCoordinates(lat, lon, apiKey, lang);
    }

    //Прогноз на N дней
    // http://localhost:8080/weather/forecast?city=Moscow&days=7&apiKey=basic-f27c60ec-811046022
    // ограничение функционала - http://localhost:8080/weather/forecast?city=Moscow&days=7&apiKey=free-5064c80f-130987652
    // дней < 1 - http://localhost:8080/weather/forecast?city=Moscow&days=0&apiKey=basic-f27c60ec-811046022
    // дней > 90 http://localhost:8080/weather/forecast?city=Moscow&days=100&apiKey=basic-f27c60ec-811046022
    @GetMapping("/forecast")
    public ForecastResponse getForecast(@RequestParam @NotBlank String city,
                                        @RequestParam(defaultValue = "7") @Min(1) @Max(90) int days,
                                        @RequestParam String apiKey,
                                        @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getForecast(city, days, apiKey, lang);
    }

    //Исторические данные за период
    // http://localhost:8080/weather/history?city=Moscow&start=2026-01-01&end=2026-01-07&apiKey=basic-f27c60ec-811046022
    // ограничение функционала - http://localhost:8080/weather/history?city=Moscow&start=2024-01-01&end=2024-01-20&apiKey=basic-f27c60ec-811046022
    @GetMapping("/history")
    public HistoricalResponse getHistoricalData(@RequestParam @NotBlank String city,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                                @RequestParam String apiKey,
                                                @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getHistoricalData(city, start.toString(), end.toString(), apiKey, lang);
    }

    //Исторические данные о погоде в конкретную дату
    // http://localhost:8080/weather/history/date?city=Moscow&date=2026-01-01&apiKey=basic-f27c60ec-811046022
    @GetMapping("/history/date")
    public HistoricalResponse getHistoricalDataByDate(@RequestParam @NotBlank String city,
                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                      @RequestParam String apiKey,
                                                      @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getHistoricalData(city, date.toString(), date.toString(), apiKey, lang);
    }

    //Погода в конкретное время
    // http://localhost:8080/weather/current/time?city=Moscow&datetime=2026-01-01T13:00:00&apiKey=premium-4fde29ef-1676466811
    @GetMapping("/current/time")
    public WeatherResponse getWeatherAtTime(@RequestParam @NotBlank String city,
                                            @RequestParam String datetime,
                                            @RequestParam String apiKey,
                                            @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getWeatherAtTime(city, datetime, apiKey, lang);
    }

    //Почасовой прогноз погоды
    // http://localhost:8080/weather/forecast/hourly?city=Moscow&date=2026-01-01&apiKey=premium-4fde29ef-1676466811
    @GetMapping("/forecast/hourly")
    public HourlyForecastResponse getHourlyForecast(@RequestParam @NotBlank String city,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                    @RequestParam String apiKey,
                                                    @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getHourlyForecast(city, date.toString(), apiKey, lang);
    }

    // Фильтрация прогноза погоды по погодным условиям
    // http://localhost:8080/weather/forecast/filter?city=Sochi&days=7&filterCondition=rain&apiKey=basic-f27c60ec-811046022
    // http://localhost:8080/weather/forecast/filter?city=Ekaterinburg&days=7&filterCondition=cloud&apiKey=basic-f27c60ec-811046022
    @GetMapping("/forecast/filter")
    public ForecastResponse getForecastWithFilter(
            @RequestParam @NotBlank String city,
            @RequestParam(defaultValue = "7") @Min(1) @Max(90) int days,
            @RequestParam(defaultValue = "ru") String lang,
            @RequestParam(required = false) String filterCondition, // например: rain, sun, snow, cloud
            @RequestParam String apiKey) {

        return weatherService.getForecastWithFilter(city, days, apiKey, lang, filterCondition);
    }
}