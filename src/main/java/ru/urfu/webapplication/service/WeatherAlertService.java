package ru.urfu.webapplication.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WeatherAlertService {
    @Value("${weather.alert.heat-threshold}")
    private double heatThreshold;
    @Value("${weather.alert.cold-threshold}")
    private double coldThreshold;
    @Value("${weather.alert.wind-threshold}")
    private double windThreshold;

    public String checkAlert(Double tempMax, Double tempMin, Double windSpeed, String conditions) {
        String alert = null;
        if (tempMax > heatThreshold) {
            alert = "Жара: " + tempMax + " градусов";
        } else if (tempMin < coldThreshold) {
            alert = "Сильный мороз: " + tempMin + " градусов";
        } else if (windSpeed > windThreshold) {
            alert = "Сильный ветер: " + windSpeed + " м/с";
        } else if (conditions != null) {
            String cond = conditions.toLowerCase();
            if (cond.contains("rain") || cond.contains("дождь") ||
                    cond.contains("snow") || cond.contains("снег")) {
                alert = "Осадки: " + conditions;
            }
        }
        return alert;
    }
}
