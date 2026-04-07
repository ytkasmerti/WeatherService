package ru.urfu.webapplication.dto;

import lombok.Data;
import java.util.List;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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