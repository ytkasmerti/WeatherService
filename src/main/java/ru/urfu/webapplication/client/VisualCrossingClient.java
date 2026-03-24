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

    public VisualCrossingResponse getWeatherData(String location) {
        System.out.println("Вызов Visual Crossing API для: " + location);

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
}