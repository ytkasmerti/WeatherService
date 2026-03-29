package ru.urfu.webapplication.dto;

import lombok.Data;
import java.util.List;

@Data
public class ForecastResponse {
    private String location;
    private List<DailyForecast> daily;

    @Data
    public static class DailyForecast {
        private String date;
        private Double tempMax;
        private Double tempMin;
        private Double tempAvg;
        private Integer humidityAvg;
        private String conditions;

        public DailyForecast(String date, Double tempMax, Double tempMin,
                             Double tempAvg, Integer humidityAvg, String conditions) {
            this.date = date;
            this.tempMax = tempMax;
            this.tempMin = tempMin;
            this.tempAvg = tempAvg;
            this.humidityAvg = humidityAvg;
            this.conditions = conditions;
        }
    }

    public ForecastResponse(String location, List<DailyForecast> daily) {
        this.location = location;
        this.daily = daily;
    }
}