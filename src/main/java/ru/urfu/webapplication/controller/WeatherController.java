package ru.urfu.webapplication.controller;

import org.springframework.web.bind.annotation.*;
import ru.urfu.webapplication.dto.WeatherResponse;
import ru.urfu.webapplication.service.WeatherService;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    // http://localhost:8080/weather/test
    @GetMapping("/test")
    public String test() {
        System.out.println("Тестовый запуск");
        return "Работает";
    }

    // http://localhost:8080/weather/current?city=Moscow
    @GetMapping("/current")
    public WeatherResponse getCurrentWeather(@RequestParam String city) {
        System.out.println("Контроллер: запрос погоды для города " + city);
        return weatherService.getCurrentWeather(city);
    }
}