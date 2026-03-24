package ru.urfu.webapplication.dto;

import lombok.Data;

@Data
public class WeatherResponse {
    private String location;
    private Double temperature;
    private Integer humidity;
    private Double windSpeed;
    private Double pressure;
    private String conditions;
    private String timestamp;

    public WeatherResponse(String location, Double temperature, Integer humidity,
                           Double windSpeed, Double pressure, String conditions, String timestamp) {
        this.location = location;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
        this.conditions = conditions;
        this.timestamp = timestamp;
    }
}