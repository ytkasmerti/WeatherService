package ru.urfu.webapplication.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.urfu.webapplication.entity.WeatherAlert;
import ru.urfu.webapplication.event.WeatherAlertEvent;
import ru.urfu.webapplication.repository.WeatherAlertRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherAlertListener {
    private final WeatherAlertRepository weatherAlertRepository;

    @EventListener
    public void handleWeatherAlert(WeatherAlertEvent event) {
        log.warn("Погодное предупреждение для города {} на {}: {}",
                event.getCity(), event.getDate(), event.getMessage());

        WeatherAlert alert = new WeatherAlert();
        alert.setCity(event.getCity());
        alert.setMessage(event.getMessage());
        alert.setAlertDate(event.getDate());
        alert.setCreatedAt(LocalDateTime.now());
        weatherAlertRepository.save(alert);
    }
}