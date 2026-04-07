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
public class HistoricalResponse {
    private String location;
    private String startDate;
    private String endDate;
    private List<DailyHistory> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyHistory {
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