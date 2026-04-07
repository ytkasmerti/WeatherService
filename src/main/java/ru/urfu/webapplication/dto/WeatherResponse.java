package ru.urfu.webapplication.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeatherResponse {
    private String location;
    private Double temperature;
    private Double feelsLike;
    private Integer humidity;
    private Double windSpeed;
    private Double windDirection;
    private Double pressure;
    private String conditions;
    private String timestamp;
}