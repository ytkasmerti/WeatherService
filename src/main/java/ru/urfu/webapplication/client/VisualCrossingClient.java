package ru.urfu.webapplication.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.urfu.webapplication.dto.visualcrossingapi.VisualCrossingResponse;

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
    public VisualCrossingResponse getCurrentWeather(String location, String lang) {
        log.info("Вызов Visual Crossing API для текущей погоды для города {}", location);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{location}")
                        .queryParam("unitGroup", "metric")
                        .queryParam("key", apiKey)
                        .queryParam("include", "current")
                        .queryParam("lang", lang) // параметр языка
                        .build(location))
                .retrieve()
                .bodyToMono(VisualCrossingResponse.class)
                .block();
    }
    //Прогноз на N дней (максимум 15 - ограничение API)
    public VisualCrossingResponse getForecast(String location, int days, String lang) {
        log.info("Вызов Visual Crossing API для прогноза на {} дней для города {}", days, location);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{location}")
                        .queryParam("unitGroup", "metric")
                        .queryParam("key", apiKey)
                        .queryParam("include", "days")
                        .queryParam("days", days)
                        .queryParam("lang", lang)
                        .build(location))
                .retrieve()
                .bodyToMono(VisualCrossingResponse.class)
                .block();
    }
    //Исторические данные
    public VisualCrossingResponse getHistoricalData(String location, String startDate, String endDate, String lang) {
        log.info("Вызов Visual Crossing API для исторических данных для {} с {} по {}", location, startDate, endDate);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{location}/{startDate}/{endDate}")
                        .queryParam("unitGroup", "metric")
                        .queryParam("key", apiKey)
                        .queryParam("include", "days")
                        .queryParam("lang", lang)
                        .build(location, startDate, endDate))
                .retrieve()
                .bodyToMono(VisualCrossingResponse.class)
                .block();
    }
    //Погода в конкретное время
    public VisualCrossingResponse getWeatherAtTime(String location, String dateTime, String lang) {
        log.info("Вызов Visual Crossing API для погоды в конкретное время для {} в {}", location, dateTime);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{location}/{dateTime}")
                        .queryParam("unitGroup", "metric")
                        .queryParam("key", apiKey)
                        .queryParam("include", "current")
                        .queryParam("lang", lang)
                        .build(location, dateTime))
                .retrieve()
                .bodyToMono(VisualCrossingResponse.class)
                .block();
    }
    //Почасовой прогноз погоды
    public VisualCrossingResponse getHourlyForecast(String location, String date, String lang) {
        log.info("Вызов Visual Crossing API для почасового прогноза для {} в день {}", location, date);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{location}/{date}")
                        .queryParam("unitGroup", "metric")
                        .queryParam("key", apiKey)
                        .queryParam("include", "hours")
                        .queryParam("lang", lang)
                        .build(location, date))
                .retrieve()
                .bodyToMono(VisualCrossingResponse.class)
                .block();
    }
}