package ru.urfu.webapplication.dto.visualcrossingapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class VisualCrossingResponse {
    @JsonProperty("resolvedAddress")
    private String resolvedAddress;
    private Double latitude;
    private Double longitude;
    private List<Day> days;
    @JsonProperty("currentConditions")
    private CurrentConditions currentConditions;
}