package ru.urfu.webapplication.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.urfu.webapplication.dto.visualcrossingapi.VisualCrossingResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        log.info("Вызов Visual Crossing API для текущей погоды: {}", location);
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
    //Прогноз на N дней (максимум 15 - ограничение API)
    public VisualCrossingResponse getForecast(String location, int days) {
        log.info("Вызов Visual Crossing API для прогноза на {} дней: {}", days, location);
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
        log.info("Вызов Visual Crossing API для исторических данных: {} с {} по {}", location, startDate, endDate);
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