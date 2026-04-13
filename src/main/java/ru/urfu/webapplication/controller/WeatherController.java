package ru.urfu.webapplication.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.urfu.webapplication.dto.ForecastResponse;
import ru.urfu.webapplication.dto.HistoricalResponse;
import ru.urfu.webapplication.dto.HourlyForecastResponse;
import ru.urfu.webapplication.dto.WeatherResponse;
import ru.urfu.webapplication.service.WeatherService;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/weather")
public class WeatherController {
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    //Текущая погода по городу
    // http://localhost:8080/weather/current?city=Moscow
    // Пустой город - http://localhost:8080/weather/current?city=
    @GetMapping("/current")
    public WeatherResponse getCurrentWeather(@RequestParam @NotBlank String city,
                                             @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getCurrentWeatherByCity(city, lang);
    }

    //Текущая погода по координатам
    // http://localhost:8080/weather/current?lat=55.75&lon=37.62&lang=en
    @GetMapping(value = "/current", params = {"lat", "lon"})
    public WeatherResponse getCurrentWeatherByCoordinates(@RequestParam @Min(-90) @Max(90) Double lat,
                                                          @RequestParam @Min(-180) @Max(180) Double lon,
                                                          @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getCurrentWeatherByCoordinates(lat, lon, lang);
    }

    //Прогноз на N дней
    // http://localhost:8080/weather/forecast?city=Moscow&days=7
    // дней < 1 - http://localhost:8080/weather/forecast?city=Moscow&days=0
    // http://localhost:8080/weather/forecast?city=Moscow&days=100
    @GetMapping("/forecast")
    public ForecastResponse getForecast(@RequestParam @NotBlank String city,
                                        @RequestParam(defaultValue = "7") @Min(1) int days,
                                        @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getForecast(city, days, lang);
    }

    //Исторические данные за период
    // http://localhost:8080/weather/history?city=Moscow&start=2026-01-01&end=2026-01-07
    @GetMapping("/history")
    public HistoricalResponse getHistoricalData(
            @RequestParam @NotBlank String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getHistoricalData(city, start.toString(), end.toString(), lang);
    }

    //Исторические данные о погоде в конкретную дату
    // http://localhost:8080/weather/history/date?city=Moscow&date=2026-01-01
    // некорректная широта http://localhost:8080/weather/current?lat=100&lon=37.62
    // некорректная долгота http://localhost:8080/weather/current?lat=55.75&lon=200
    @GetMapping("/history/date")
    public HistoricalResponse getHistoricalDataByDate(
            @RequestParam @NotBlank String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getHistoricalData(city, date.toString(), date.toString(), lang);
    }

    //Погода в конкретное время
    // http://localhost:8080/weather/current/time?city=Moscow&datetime=2026-01-01T13:00:00
    @GetMapping("/current/time")
    public WeatherResponse getWeatherAtTime(
            @RequestParam @NotBlank String city,
            @RequestParam String datetime,
            @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getWeatherAtTime(city, datetime, lang);
    }

    //Почасовой прогноз погоды
    // http://localhost:8080/weather/forecast/hourly?city=Moscow&date=2026-01-01
    // http://localhost:8080/weather/history?city=Moscow&start=2024-13-01&end=2024-01-07
    @GetMapping("/forecast/hourly")
    public HourlyForecastResponse getHourlyForecast(
            @RequestParam @NotBlank String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "ru") String lang) {
        return weatherService.getHourlyForecast(city, date.toString(), lang);
    }
}