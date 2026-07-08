package ru.urfu.webapplication.dto.visualcrossingapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CurrentConditions {
    private Double temp;
    @JsonProperty("feelslike")
    private Double feelsLike;
    private Double humidity;
    @JsonProperty("windspeed")
    private Double windSpeed;
    @JsonProperty("winddir")
    private Double windDirection;
    private Double pressure;
    private String conditions;
}