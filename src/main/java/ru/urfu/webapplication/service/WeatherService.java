package ru.urfu.webapplication.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.urfu.webapplication.client.VisualCrossingClient;
import ru.urfu.webapplication.dto.ForecastResponse;
import ru.urfu.webapplication.dto.HistoricalResponse;
import ru.urfu.webapplication.dto.HourlyForecastResponse;
import ru.urfu.webapplication.dto.WeatherResponse;
import ru.urfu.webapplication.dto.visualcrossingapi.Day;
import ru.urfu.webapplication.dto.visualcrossingapi.Hour;
import ru.urfu.webapplication.dto.visualcrossingapi.VisualCrossingResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import ru.urfu.webapplication.event.WeatherAlertEvent;
import ru.urfu.webapplication.model.SubscriptionLevel;

@Slf4j
@Service
public class WeatherService {
    private final VisualCrossingClient visualCrossingClient;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ApplicationEventPublisher eventPublisher;
    private final ApiKeyService apiKeyService;

    public WeatherService(VisualCrossingClient visualCrossingClient, ApplicationEventPublisher eventPublisher, ApiKeyService apiKeyService) {
        this.visualCrossingClient = visualCrossingClient;
        this.eventPublisher = eventPublisher;
        this.apiKeyService = apiKeyService;
    }

    //Регистрация пользователя
    public Map<String, String> registerUser(String email, String plan) {
        String apiKey = apiKeyService.generateApiKey(email, plan);
        log.info("Пользователь {} успешно зарегистрирован. Сгенерирован API ключ: {}", email, apiKey);
        Map<String, String> response = new HashMap<>();
        response.put("apiKey", apiKey);
        response.put("subscription", plan);
        response.put("message", "Сохраните ваш API ключ!");
        return response;
    }

    //Проверка погодных предупреждений
    private void checkAndPublishAlert(String city, String date, Double tempMax, Double tempMin, Double windSpeed, String conditions) {
        String alert = null;
        if (tempMax > 30) {
            alert = "Жара: " + tempMax + "градусов";
        } else if (tempMin < -20) {
            alert = "Сильный мороз: " + tempMin + "градусов";
        } else if (windSpeed > 15) {
            alert = "Сильный ветер: " + windSpeed + " м/с";
        } else if (conditions != null) {
            String cond = conditions.toLowerCase();
            if (cond.contains("rain") ||
                    cond.contains("дождь") ||
                    cond.contains("snow") ||
                    cond.contains("снег")) {
                alert = "Осадки: " + conditions;
            }
        }
        if (alert != null) {
            eventPublisher.publishEvent(new WeatherAlertEvent(this, city, alert, date));
        }
    }

    //Общий метод для получения текущей погоды
    private WeatherResponse fetchWeatherFromApi(String location, String lang) {
        log.info("Вызов API для получения погоды по локации {}", location);
        VisualCrossingResponse response = visualCrossingClient.getCurrentWeather(location, lang);
        checkAndPublishAlert(location, "текущее время",
                response.getCurrentConditions().getTemp(),
                response.getCurrentConditions().getTemp(),
                response.getCurrentConditions().getWindSpeed(),
                response.getCurrentConditions().getConditions());

        return new WeatherResponse(
                response.getResolvedAddress(),
                response.getCurrentConditions().getTemp(),
                response.getCurrentConditions().getFeelsLike(),
                response.getCurrentConditions().getHumidity() != null ?
                        response.getCurrentConditions().getHumidity().intValue() : null,
                response.getCurrentConditions().getWindSpeed(),
                response.getCurrentConditions().getWindDirection(),
                response.getCurrentConditions().getPressure(),
                response.getCurrentConditions().getConditions(),
                LocalDateTime.now().format(formatter)
        );
    }

    //Текущая погода по городу (доступ free+)
    public WeatherResponse getCurrentWeatherByCity(String city, String apiKey, String lang) {
        validateAndGetLevel(apiKey);
        log.info("Запрос текущей погоды для города {}", city);
        return fetchWeatherFromApi(city, lang);
    }

    //Текущая погода по координатам (доступ free+)
    public WeatherResponse getCurrentWeatherByCoordinates(Double lat, Double lon, String apiKey, String lang) {
        validateAndGetLevel(apiKey);
        String location = lat + "," + lon;
        log.info("Запрос текущей погоды по координатам {}", location);
        return fetchWeatherFromApi(location, lang);
    }

    // фильтрация
    public ForecastResponse getForecastWithFilter(String city, int days, String apiKey, String lang, String filterCondition) {
        ForecastResponse forecast = getForecast(city, days, apiKey, lang);
        if (filterCondition == null || filterCondition.isEmpty()) {
            return forecast;
        }
        // словарь соответствий
        Map<String, List<String>> dict = new HashMap<>();
        dict.put("дождь", List.of("дождь", "rain"));
        dict.put("солнце", List.of("солнечно", "ясно", "sun", "clear"));
        dict.put("облачно", List.of("облачно", "пасмурно", "cloud", "overcast"));
        dict.put("снег", List.of("снег", "snow"));
        dict.put("ветер", List.of("ветер", "wind"));

        String filterLower = filterCondition.trim().toLowerCase();
        List<ForecastResponse.DailyForecast> filtered = new ArrayList<>();
        for (ForecastResponse.DailyForecast day : forecast.getDaily()) {
            if (day.getConditions().toLowerCase().contains(filterCondition.toLowerCase())) {
                filtered.add(day);
            }
        }
        forecast.setDaily(filtered);
        log.info("Отфильтровано: из {} дней оставлено {}", forecast.getDaily().size(), filtered.size());
        return forecast;
    }
    //Прогноз на N дней (доступ basic+)
    public ForecastResponse getForecast(String city, int days, String apiKey, String lang) {
        //Ограничение прогноза 15 днями (максимум API)
        SubscriptionLevel level = validateAndGetLevel(apiKey);
        if (level == SubscriptionLevel.FREE) {
            throw new RuntimeException("Прогноз погоды доступен только с BASIC или PREMIUM подпиской");
        }
        int validDays = Math.min(days, 15);
        if (days > 15) {
            log.warn("Запрошено {} дней, ограничено 15 днями", days);
        }
        log.info("Запрос прогноза погоды на {} дней для города {}", validDays, city);
        VisualCrossingResponse response = visualCrossingClient.getForecast(city, validDays, lang);
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
                        day.getFeelsLike(),
                        day.getHumidity() != null ? day.getHumidity().intValue() : null,
                        day.getWindSpeed(),
                        day.getWindDirection(),
                        day.getPressure(),
                        day.getConditions(),
                        day.getUvIndex(),
                        day.getSunrise(),
                        day.getSunset()
                ));
                checkAndPublishAlert(city, day.getDatetime(), day.getTempMax(), day.getTempMin(), day.getWindSpeed(), day.getConditions());
            }
            log.info("Возвращено {} дней прогноза", dailyList.size());
        }
        return new ForecastResponse(response.getResolvedAddress(), dailyList);
    }

    //Исторические данные за период (доступ до 7 дней basic+, доступ больше 7 дней - premium)
    public HistoricalResponse getHistoricalData(String city, String startDate, String endDate, String apiKey, String lang) {
        SubscriptionLevel level = validateAndGetLevel(apiKey);
        if (level == SubscriptionLevel.FREE) {
            throw new RuntimeException("Прогноз погоды доступен только с BASIC или PREMIUM подпиской");
        }

        if (level == SubscriptionLevel.BASIC) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
            if (daysBetween > 7) {
                throw new RuntimeException("BASIC подписка позволяет запрашивать историю не более чем на 7 дней");
            }
        }
        log.info("Запрос истории погоды для города {} с {} по {}", city, startDate, endDate);
        VisualCrossingResponse response = visualCrossingClient.getHistoricalData(city, startDate, endDate, lang);
        List<HistoricalResponse.DailyHistory> historyList = new ArrayList<>();
        if (response.getDays() != null) {
            for (Day day : response.getDays()) {
                historyList.add(new HistoricalResponse.DailyHistory(
                        day.getDatetime(),
                        day.getTempMax(),
                        day.getTempMin(),
                        day.getTemp(),
                        day.getFeelsLike(),
                        day.getHumidity() != null ? day.getHumidity().intValue() : null,
                        day.getWindSpeed(),
                        day.getWindDirection(),
                        day.getPressure(),
                        day.getConditions(),
                        day.getUvIndex(),
                        day.getSunrise(),
                        day.getSunset()
                ));
                checkAndPublishAlert(city, day.getDatetime(), day.getTempMax(), day.getTempMin(), day.getWindSpeed(), day.getConditions());
            }
        }
        return new HistoricalResponse(
                response.getResolvedAddress(),
                startDate,
                endDate,
                historyList
        );
    }

    //Погода в конкретное время (доступ premium)
    public WeatherResponse getWeatherAtTime(String city, String dateTime, String apiKey, String lang) {
        SubscriptionLevel level = validateAndGetLevel(apiKey);
        if (level != SubscriptionLevel.PREMIUM) {
            throw new RuntimeException("Погода на конкретное время доступна только с PREMIUM подпиской");
        }
        log.info("Запрос погоды для города {} на время {}", city, dateTime);
        VisualCrossingResponse response = visualCrossingClient.getWeatherAtTime(city, dateTime, lang);
        checkAndPublishAlert(city, dateTime,
                response.getCurrentConditions().getTemp(),
                response.getCurrentConditions().getTemp(),
                response.getCurrentConditions().getWindSpeed(),
                response.getCurrentConditions().getConditions());

        return new WeatherResponse(
                response.getResolvedAddress(),
                response.getCurrentConditions().getTemp(),
                response.getCurrentConditions().getFeelsLike(),
                response.getCurrentConditions().getHumidity() != null ?
                        response.getCurrentConditions().getHumidity().intValue() : null,
                response.getCurrentConditions().getWindSpeed(),
                response.getCurrentConditions().getWindDirection(),
                response.getCurrentConditions().getPressure(),
                response.getCurrentConditions().getConditions(),
                dateTime
        );
    }

    //Почасовой прогноз погоды (доступ premium)
    public HourlyForecastResponse getHourlyForecast(String city, String date, String apiKey, String lang) {
        SubscriptionLevel level = validateAndGetLevel(apiKey);
        if (level != SubscriptionLevel.PREMIUM) {
            throw new RuntimeException("Почасовой прогноз доступен только с PREMIUM подпиской");
        }
        log.info("Запрос почасового прогноза для города {} на {}", city, date);
        VisualCrossingResponse response = visualCrossingClient.getHourlyForecast(city, date, lang);
        List<HourlyForecastResponse.HourlyData> hourlyList = new ArrayList<>();
        if (response.getDays() != null && !response.getDays().isEmpty()) {
            Day day = response.getDays().getFirst();
            if (day.getHours() != null) {
                for (Hour hour : day.getHours()) {
                    hourlyList.add(HourlyForecastResponse.HourlyData.builder()
                            .time(hour.getDatetime())
                            .temperature(hour.getTemp())
                            .feelsLike(hour.getFeelsLike())
                            .humidity(hour.getHumidity() != null ? hour.getHumidity().intValue() : null)
                            .windSpeed(hour.getWindSpeed())
                            .windDirection(hour.getWindDirection())
                            .pressure(hour.getPressure())
                            .conditions(hour.getConditions())
                            .uvIndex(hour.getUvIndex())
                            .build());
                    checkAndPublishAlert(city, hour.getDatetime(), hour.getTemp(), day.getTemp(), hour.getWindSpeed(), hour.getConditions());
                }
            }
        }
        return HourlyForecastResponse.builder()
                .location(response.getResolvedAddress())
                .date(date)
                .hours(hourlyList)
                .build();
    }

    private SubscriptionLevel validateAndGetLevel(String apiKey) {
        if (!apiKeyService.isValidKey(apiKey)) {
            throw new RuntimeException("Неверный API ключ");
        }
        SubscriptionLevel level = apiKeyService.getSubscriptionLevel(apiKey);
        if (level == null) {
            throw new RuntimeException("Неверный API ключ");
        }
        return level;
    }
}