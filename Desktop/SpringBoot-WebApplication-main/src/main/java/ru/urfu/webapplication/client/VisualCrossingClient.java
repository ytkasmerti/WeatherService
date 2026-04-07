package ru.urfu.webapplication.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.urfu.webapplication.dto.visualcrossingapi.VisualCrossingResponse;

@Component
public class VisualCrossingClient {
    private final WebClient webClient;
    @Value("${weather.api.key}")
    private String apiKey;

    public VisualCrossingClient(WebClient webClient) {
        this.webClient = webClient;
    }
    //Текущая погода
    public VisualCrossingResponse getCurrentWeather(String location) {
        System.out.println("Вызов Visual Crossing API для текущей погоды: " + location);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{location}")
                        .queryParam("unitGroup", "metric")
                        .queryParam("key", apiKey)
                        .queryParam("include", "current")
                        .build(location))
                .retrieve()
                .bodyToMono(VisualCrossingResponse.class)
                .block();
    }
    //Прогноз на N дней (максимум 15)
    public VisualCrossingResponse getForecast(String location, int days) {
        System.out.println("Вызов Visual Crossing API для прогноза на " + days + " дней: " + location);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{location}")
                        .queryParam("unitGroup", "metric")
                        .queryParam("key", apiKey)
                        .queryParam("include", "days")
                        .queryParam("days", days)
                        .build(location))
                .retrieve()
                .bodyToMono(VisualCrossingResponse.class)
                .block();
    }
    //Исторические данные
    public VisualCrossingResponse getHistoricalData(String location, String startDate, String endDate) {
        System.out.println("Вызов Visual Crossing API для исторических данных: " + location + " с " + startDate + " по " + endDate);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{location}/{startDate}/{endDate}")
                        .queryParam("unitGroup", "metric")
                        .queryParam("key", apiKey)
                        .queryParam("include", "days")
                        .build(location, startDate, endDate))
                .retrieve()
                .bodyToMono(VisualCrossingResponse.class)
                .block();
    }
}