package ru.urfu.webapplication.dto.visualcrossingapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Day {
    private String datetime;
    private Double temp;
    @JsonProperty("tempmax")
    private Double tempMax;
    @JsonProperty("tempmin")
    private Double tempMin;
    @JsonProperty("feelslike")
    private Double feelsLike;
    private Double humidity;
    @JsonProperty("windspeed")
    private Double windSpeed;
    @JsonProperty("winddir")
    private Double windDirection;
    private Double pressure;
    private String conditions;
    @JsonProperty("uvindex")
    private Integer uvIndex;
    private String sunrise;
    private String sunset;
    private List<Hour> hours;
}