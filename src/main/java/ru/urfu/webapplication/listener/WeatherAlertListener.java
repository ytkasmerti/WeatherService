package ru.urfu.webapplication.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.urfu.webapplication.event.WeatherAlertEvent;

@Slf4j
@Component
public class WeatherAlertListener {

    @EventListener
    public void handleWeatherAlert(WeatherAlertEvent event) {
        log.warn("Погодное предупреждение для города {} на {}: {}",
                event.getCity(), event.getDate(), event.getMessage());
    }
}