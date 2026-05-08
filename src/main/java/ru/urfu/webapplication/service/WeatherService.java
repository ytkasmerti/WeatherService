package ru.urfu.webapplication.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
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
import ru.urfu.webapplication.entity.WeatherRequest;
import ru.urfu.webapplication.event.WeatherAlertEvent;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.WeatherRequestRepository;

@Slf4j
@Service
public class WeatherService {
    private final VisualCrossingClient visualCrossingClient;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ApplicationEventPublisher eventPublisher;
    private final ApiKeyService apiKeyService;
    private final DtoMapperService mapper;
    private final WeatherRequestRepository weatherRequestRepository;
    @Value("${systemApiKey}")
    private String apiKey;

    public WeatherService(VisualCrossingClient visualCrossingClient,
                          ApplicationEventPublisher eventPublisher,
                          ApiKeyService apiKeyService,
                          DtoMapperService mapper, WeatherRequestRepository weatherRequestRepository) {
        this.visualCrossingClient = visualCrossingClient;
        this.eventPublisher = eventPublisher;
        this.apiKeyService = apiKeyService;
        this.mapper = mapper;
        this.weatherRequestRepository = weatherRequestRepository;
    }

    //Метод проверки ключа и уровня доступа
    private SubscriptionLevel validateAndGetLevel(String apiKey) {
        if (!apiKeyService.isValidKey(apiKey)) {
            throw new RuntimeException("Неверный API ключ");
        }
        SubscriptionLevel level = apiKeyService.getSubscriptionLevel(apiKey);
        if (level == null) {
            throw new RuntimeException("Неверный API ключ");
        }
        //проверка лимитов
        if (!apiKeyService.canMakeRequest(apiKey)) {
            throw new RuntimeException("Превышен лимит запросов на сегодня");
        }
        return level;
    }

    private void saveRequest(String city, String requestType, String apiKey) {
        WeatherRequest request = new WeatherRequest();
        request.setCity(city);
        request.setRequestType(requestType);
        request.setRequestTime(LocalDateTime.now());
        request.setApiKey(apiKey);
        weatherRequestRepository.save(request);
        log.debug("Сохранён запрос {} для города {}, ключ: {}", requestType, city, apiKey);
    }

    //Проверка погодных предупреждений
    private String checkAndPublishAlert(String city, String date, Double tempMax, Double tempMin, Double windSpeed, String conditions) {
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
        return alert;
    }

    //Проверка погодных условий для планировщика
    public String checkWeatherConditions(String city, int days, String lang) {
        ForecastResponse forecast = getForecast(city, days, apiKey, lang);
        ForecastResponse.DailyForecast today = forecast.getDaily().getFirst();
        return checkAndPublishAlert(city, "сегодня",
                today.getTempMax(), today.getTempMin(),
                today.getWindSpeed(), today.getConditions());
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
        return mapper.toWeatherResponse(response, location, lang);
    }

    //Текущая погода по городу (доступ free+)
    public WeatherResponse getCurrentWeatherByCity(String city, String apiKey, String lang) {
        validateAndGetLevel(apiKey);
        log.info("Запрос текущей погоды для города {}", city);
        saveRequest(city, "current", apiKey);
        return fetchWeatherFromApi(city, lang);
    }

    //Текущая погода по координатам (доступ free+)
    public WeatherResponse getCurrentWeatherByCoordinates(Double lat, Double lon, String apiKey, String lang) {
        validateAndGetLevel(apiKey);
        String location = lat + "," + lon;
        log.info("Запрос текущей погоды по координатам {}", location);
        saveRequest(location, "current", apiKey);
        return fetchWeatherFromApi(location, lang);
    }

    // фильтрация (доступ premium)
    @PreAuthorize("hasRole('PREMIUM')")
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

        List<ForecastResponse.DailyForecast> filtered = new ArrayList<>();
        for (ForecastResponse.DailyForecast day : forecast.getDaily()) {
            if (day.getConditions().toLowerCase().contains(filterCondition.toLowerCase())) {
                filtered.add(day);
            }
        }
        forecast.setDaily(filtered);
        log.info("Отфильтровано: из {} дней оставлено {}", forecast.getDaily().size(), filtered.size());
        saveRequest(city, "forecast", apiKey);
        return forecast;
    }

    //Прогноз на N дней (доступ basic+)
    @PreAuthorize("hasRole('BASIC') or hasRole('PREMIUM')")
    public ForecastResponse getForecast(String city, int days, String apiKey, String lang) {
        validateAndGetLevel(apiKey);
        //Ограничение прогноза 15 днями (максимум API)
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
                dailyList.add(mapper.toDailyForecast(day));
                checkAndPublishAlert(city, day.getDatetime(), day.getTempMax(), day.getTempMin(), day.getWindSpeed(), day.getConditions());
            }
            log.info("Возвращено {} дней прогноза", dailyList.size());
        }
        saveRequest(city, "filteredForecast", apiKey);
        return mapper.toForecastResponse(response, dailyList);
    }

    //Исторические данные за период (доступ до 7 дней basic+, доступ больше 7 дней - premium)
    public HistoricalResponse getHistoricalData(String city, String startDate, String endDate, String apiKey, String lang) {
        SubscriptionLevel level = validateAndGetLevel(apiKey);
        if (level == SubscriptionLevel.FREE) {
            throw new RuntimeException("Ваш тариф не позволяет использовать эту функцию. Повысьте уровень подписки");
        }

        if (level == SubscriptionLevel.BASIC) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
            if (daysBetween > 7) {
                throw new RuntimeException("BASIC подписка позволяет запрашивать историю не более чем на 7 дней. Повысьте уровень подписки");
            }
        }
        log.info("Запрос истории погоды для города {} с {} по {}", city, startDate, endDate);
        VisualCrossingResponse response = visualCrossingClient.getHistoricalData(city, startDate, endDate, lang);
        List<HistoricalResponse.DailyHistory> historyList = new ArrayList<>();
        if (response.getDays() != null) {
            for (Day day : response.getDays()) {
                historyList.add(mapper.toDailyHistory(day));
                checkAndPublishAlert(city, day.getDatetime(), day.getTempMax(), day.getTempMin(), day.getWindSpeed(), day.getConditions());
            }
        }
        saveRequest(city, "history", apiKey);
        return mapper.toHistoricalResponse(response, startDate, endDate, historyList);
    }

    //Погода в конкретное время (доступ premium)
    @PreAuthorize("hasRole('PREMIUM')")
    public WeatherResponse getWeatherAtTime(String city, String dateTime, String apiKey, String lang) {
        validateAndGetLevel(apiKey);
        log.info("Запрос погоды для города {} на время {}", city, dateTime);
        VisualCrossingResponse response = visualCrossingClient.getWeatherAtTime(city, dateTime, lang);
        checkAndPublishAlert(city, dateTime,
                response.getCurrentConditions().getTemp(),
                response.getCurrentConditions().getTemp(),
                response.getCurrentConditions().getWindSpeed(),
                response.getCurrentConditions().getConditions());
        saveRequest(city, "weatherAtTime", apiKey);
        return mapper.toWeatherResponse(response, city, dateTime);
    }

    //Почасовой прогноз погоды (доступ premium)
    @PreAuthorize("hasRole('PREMIUM')")
    public HourlyForecastResponse getHourlyForecast(String city, String date, String apiKey, String lang) {
        validateAndGetLevel(apiKey);
        log.info("Запрос почасового прогноза для города {} на {}", city, date);
        VisualCrossingResponse response = visualCrossingClient.getHourlyForecast(city, date, lang);
        List<HourlyForecastResponse.HourlyData> hourlyList = new ArrayList<>();
        if (response.getDays() != null && !response.getDays().isEmpty()) {
            Day day = response.getDays().getFirst();
            if (day.getHours() != null) {
                for (Hour hour : day.getHours()) {
                    hourlyList.add(mapper.toHourlyData(hour));
                    checkAndPublishAlert(city, hour.getDatetime(), hour.getTemp(), day.getTemp(), hour.getWindSpeed(), hour.getConditions());
                }
            }
        }
        saveRequest(city, "hourlyForecast", apiKey);
        return mapper.toHourlyForecastResponse(response, date, hourlyList);
    }
}