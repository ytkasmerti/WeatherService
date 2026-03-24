package ru.urfu.webapplication.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.dto.ForecastResponse;
import ru.urfu.webapplication.dto.HistoricalResponse;
import ru.urfu.webapplication.dto.WeatherResponse;
import ru.urfu.webapplication.service.WeatherService;
import java.time.LocalDate;

@RestController
@RequestMapping("/weather")
public class WeatherController {
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
    //Текущая погода по городу
    // http://localhost:8080/weather/current?city=Moscow
    @GetMapping("/current")
    public WeatherResponse getCurrentWeather(@RequestParam String city) {
        System.out.println("WeatherController: запрос текущей погоды для города " + city);
        return weatherService.getCurrentWeatherByCity(city);
    }

    //Текущая погода по координатам
    // http://localhost:8080/weather/current?lat=55.75&lon=37.62
    @GetMapping(value = "/current", params = {"lat", "lon"})
    public WeatherResponse getCurrentWeatherByCoordinates(
            @RequestParam Double lat,
            @RequestParam Double lon) {
        System.out.println("WeatherController: запрос текущей погоды по координатам " + lat + ", " + lon);
        return weatherService.getCurrentWeatherByCoordinates(lat, lon);
    }

    //Прогноз на N дней
    // http://localhost:8080/weather/forecast?city=Moscow&days=7
    @GetMapping("/forecast")
    public ForecastResponse getForecast(
            @RequestParam String city,
            @RequestParam(defaultValue = "7") int days) {
        System.out.println("WeatherController: запрос прогноза на " + days + " дней для " + city);

        if (days > 15) {
            days = 15;
            System.out.println("Прогноз ограничен 15 днями");
        }
        return weatherService.getForecast(city, days);
    }

    //Исторические данные за период
    // http://localhost:8080/weather/history?city=Moscow&start=2024-01-01&end=2024-01-07
    @GetMapping("/history")
    public HistoricalResponse getHistoricalData(
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        System.out.println("Контроллер: запрос истории для " + city + " с " + start + " по " + end);
        return weatherService.getHistoricalData(city, start.toString(), end.toString());
    }
}