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

@Service
public class WeatherService {
    private final VisualCrossingClient visualCrossingClient;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public WeatherService(VisualCrossingClient visualCrossingClient) {
        this.visualCrossingClient = visualCrossingClient;
    }
    //Текущая погода по городу
    public WeatherResponse getCurrentWeatherByCity(String city) {
        System.out.println("WeatherService: запрос текущей погоды для города " + city);
        return getWeatherResponse(city);
    }
    //Текущая погода по координатам
    public WeatherResponse getCurrentWeatherByCoordinates(Double lat, Double lon) {
        String location = lat + "," + lon;
        System.out.println("WeatherService: запрос текущей погоды по координатам " + location);
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
            System.out.println("Ошибка при вызове API: " + e.getMessage());
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
    //Прогноз на N дней (максимум 15)
    public ForecastResponse getForecast(String city, int days) {
        System.out.println("WeatherService: запрос прогноза на " + days + " дней для " + city);
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
                System.out.println("Возвращено " + dailyList.size() + " дней прогноза");
            }
            return new ForecastResponse(response.getResolvedAddress(), dailyList);
        } catch (Exception e) {
            System.out.println("Ошибка при получении прогноза: " + e.getMessage());
            return new ForecastResponse(city, new ArrayList<>());
        }
    }
    //Исторические данные за период
    public HistoricalResponse getHistoricalData(String city, String startDate, String endDate) {
        System.out.println("WeatherService: запрос истории для города " + city + " с " + startDate + " по " + endDate);
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
            System.out.println("Ошибка при получении истории: " + e.getMessage());
            return new HistoricalResponse(city, startDate, endDate, new ArrayList<>());
        }
    }
}