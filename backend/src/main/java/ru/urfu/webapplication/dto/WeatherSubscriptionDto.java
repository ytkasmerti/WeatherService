package ru.urfu.webapplication.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherSubscriptionDto {
    private Long id;
    private String city;
    private boolean notifyHeat;
    private boolean notifyCold;
    private boolean notifyWind;
    private boolean notifyPrecipitation;
}