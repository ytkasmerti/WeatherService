package ru.urfu.webapplication.service;

import org.springframework.stereotype.Service;
import ru.urfu.webapplication.client.VisualCrossingClient;
import ru.urfu.webapplication.dto.WeatherResponse;
import ru.urfu.webapplication.dto.visualcrossingapi.VisualCrossingResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class WeatherService {

    private final VisualCrossingClient visualCrossingClient;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public WeatherService(VisualCrossingClient visualCrossingClient) {
        this.visualCrossingClient = visualCrossingClient;
    }

    public WeatherResponse getCurrentWeather(String city) {
        System.out.println("Сервис: запрос к API для города " + city);
        try {
            VisualCrossingResponse response = visualCrossingClient.getWeatherData(city);
            return new WeatherResponse(
                    response.getResolvedAddress(),
                    response.getCurrentConditions().getTemp(),
                    response.getCurrentConditions().getHumidity().intValue(),
                    response.getCurrentConditions().getWindSpeed(),
                    response.getCurrentConditions().getPressure(),
                    response.getCurrentConditions().getConditions(),
                    LocalDateTime.now().format(formatter)
            );
        } catch (Exception e) {
            System.out.println("Ошибка при вызове API: " + e.getMessage());
            return new WeatherResponse(
                    city,
                    -9999.9,
                    -9999,
                    -9999.9,
                    -9999.9,
                    "Ошибка",
                    LocalDateTime.now().format(formatter)
            );
        }
    }
}