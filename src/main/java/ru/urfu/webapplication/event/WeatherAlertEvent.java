package ru.urfu.webapplication.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WeatherAlertEvent extends ApplicationEvent {
    private final String city;
    private final String message;
    private final String date;

    public WeatherAlertEvent(Object source, String city, String message, String date) {
        super(source);
        this.city = city;
        this.message = message;
        this.date = date;
    }
}