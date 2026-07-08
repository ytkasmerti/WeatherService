package ru.urfu.webapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForecastResponse {
    private String location;
    private List<DailyForecast> daily;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyForecast {
        private String date;
        private Double tempMax;
        private Double tempMin;
        private Double tempAvg;
        private Double feelsLike;
        private Integer humidityAvg;
        private Double windSpeed;
        private Double windDirection;
        private Double pressure;
        private String conditions;
        private Integer uvIndex;
        private String sunrise;
        private String sunset;
    }
}