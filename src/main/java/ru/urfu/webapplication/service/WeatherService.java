package ru.urfu.webapplication.service;

import org.springframework.stereotype.Service;
import ru.urfu.webapplication.client.VisualCrossingClient;
import ru.urfu.webapplication.dto.ForecastResponse;
import ru.urfu.webapplication.dto.HistoricalResponse;
import ru.urfu.webapplication.dto.WeatherResponse;
import ru.urfu.webapplication.dto.visualcrossingapi.Day;
import ru.urfu.webapplication.dto.visualcrossingapi.VisualCrossingResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WeatherService {
    private final VisualCrossingClient visualCrossingClient;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public WeatherService(VisualCrossingClient visualCrossingClient) {
        this.visualCrossingClient = visualCrossingClient;
    }
    //Текущая погода по городу
    public WeatherResponse getCurrentWeatherByCity(String city) {
        log.info("Запрос текущей погоды для города {}", city);
        return getWeatherResponse(city);
    }
    //Текущая погода по координатам
    public WeatherResponse getCurrentWeatherByCoordinates(Double lat, Double lon) {
        String location = lat + "," + lon;
        log.info("Запрос текущей погоды по координатам {}", location);
        return getWeatherResponse(location);
    }
    //Общий метод для получения текущей погоды
    private WeatherResponse getWeatherResponse(String location) {
        try {
            VisualCrossingResponse response = visualCrossingClient.getCurrentWeather(location);
            return new WeatherResponse(
                    response.getResolvedAddress(),
                    response.getCurrentConditions().getTemp(),
                    response.getCurrentConditions().getHumidity() != null ?
                            response.getCurrentConditions().getHumidity().intValue() : null,
                    response.getCurrentConditions().getWindSpeed(),
                    response.getCurrentConditions().getPressure(),
                    response.getCurrentConditions().getConditions(),
                    LocalDateTime.now().format(formatter)
            );
        } catch (Exception e) {
            log.error("Ошибка при вызове API: {}", e.getMessage());
            return new WeatherResponse(
                    location,
                    -9999.9,
                    -9999,
                    -9999.9,
                    -9999.9,
                    "Ошибка",
                    LocalDateTime.now().format(formatter)
            );
        }
    }
    //Прогноз на N дней
    public ForecastResponse getForecast(String city, int days) {
        //Ограничение прогноза 15 днями (максимум API)
        int validDays = Math.min(days, 15);
        if (days > 15) {
            log.warn("Запрошено {} дней, ограничиваем 15", days);
        }
        log.info("Запрос прогноза на {} дней для города {}", validDays, city);
        try {
            VisualCrossingResponse response = visualCrossingClient.getForecast(city, days);
            List<ForecastResponse.DailyForecast> dailyList = new ArrayList<>();
            if (response.getDays() != null) {
                int limit = Math.min(days, response.getDays().size());
                for (int i = 0; i < limit; i++) {
                    Day day = response.getDays().get(i);
                    dailyList.add(new ForecastResponse.DailyForecast(
                            day.getDatetime(),
                            day.getTempMax(),
                            day.getTempMin(),
                            day.getTemp(),
                            day.getHumidity() != null ? day.getHumidity().intValue() : null,
                            day.getConditions()
                    ));
                }
                log.info("Возвращено {} дней прогноза", dailyList.size());
            }
            return new ForecastResponse(response.getResolvedAddress(), dailyList);
        } catch (Exception e) {
            log.error("Ошибка при получении прогноза: {}", e.getMessage());
            return new ForecastResponse(city, new ArrayList<>());
        }
    }
    //Исторические данные за период
    public HistoricalResponse getHistoricalData(String city, String startDate, String endDate) {
        log.info("Запрос истории для города {} с {} по {}", city, startDate, endDate);
        try {
            VisualCrossingResponse response = visualCrossingClient.getHistoricalData(city, startDate, endDate);
            List<HistoricalResponse.DailyHistory> historyList = new ArrayList<>();
            if (response.getDays() != null) {
                for (Day day : response.getDays()) {
                    historyList.add(new HistoricalResponse.DailyHistory(
                            day.getDatetime(),
                            day.getTempMax(),
                            day.getTempMin(),
                            day.getTemp(),
                            day.getConditions()
                    ));
                }
            }
            return new HistoricalResponse(
                    response.getResolvedAddress(),
                    startDate,
                    endDate,
                    historyList
            );
        } catch (Exception e) {
            log.error("Ошибка при получении истории: {}", e.getMessage());
            return new HistoricalResponse(city, startDate, endDate, new ArrayList<>());
        }
    }
}