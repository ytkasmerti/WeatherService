package ru.urfu.webapplication.dto;

import lombok.Data;
import java.util.List;

@Data
public class HistoricalResponse {
    private String location;
    private String startDate;
    private String endDate;
    private List<DailyHistory> data;

    @Data
    public static class DailyHistory {
        private String date;
        private Double tempMax;
        private Double tempMin;
        private Double tempAvg;
        private String conditions;

        public DailyHistory(String date, Double tempMax, Double tempMin,
                            Double tempAvg, String conditions) {
            this.date = date;
            this.tempMax = tempMax;
            this.tempMin = tempMin;
            this.tempAvg = tempAvg;
            this.conditions = conditions;
        }
    }

    public HistoricalResponse(String location, String startDate, String endDate, List<DailyHistory> data) {
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.data = data;
    }
}