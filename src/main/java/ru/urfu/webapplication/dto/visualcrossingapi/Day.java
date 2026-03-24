package ru.urfu.webapplication.dto.visualcrossingapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Day {
    private String datetime;
    private Double temp;
    @JsonProperty("tempmax")
    private Double tempMax;
    @JsonProperty("tempmin")
    private Double tempMin;
    private Double humidity;
    @JsonProperty("windspeed")
    private Double windSpeed;
    private Double pressure;
    private String conditions;
}