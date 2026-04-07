package ru.urfu.webapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyForecastResponse {
    private String location;
    private String date;
    private List<HourlyData> hours;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyData {
        private String time;
        private Double temperature;
        private Double feelsLike;
        private Integer humidity;
        private Double windSpeed;
        private Double windDirection;
        private Double pressure;
        private String conditions;
        private Integer uvIndex;
    }
}