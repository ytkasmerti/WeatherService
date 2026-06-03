package ru.urfu.webapplication.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.urfu.webapplication.client.VisualCrossingClient;
import ru.urfu.webapplication.dto.ForecastResponse;
import ru.urfu.webapplication.dto.HistoricalResponse;
import ru.urfu.webapplication.dto.HourlyForecastResponse;
import ru.urfu.webapplication.dto.WeatherResponse;
import ru.urfu.webapplication.dto.visualcrossingapi.Day;
import ru.urfu.webapplication.dto.visualcrossingapi.Hour;
import ru.urfu.webapplication.dto.visualcrossingapi.VisualCrossingResponse;
import ru.urfu.webapplication.entity.WeatherRequest;
import ru.urfu.webapplication.model.RequestType;
import ru.urfu.webapplication.model.SubscriptionLevel;
import ru.urfu.webapplication.repository.WeatherRequestRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WeatherService {
    private final VisualCrossingClient visualCrossingClient;
    private final ApiKeyService apiKeyService;
    private final DtoMapperService mapper;
    private final WeatherRequestRepository weatherRequestRepository;
    private final WeatherAlertService weatherAlertService;
    @Value("${systemApiKey}")
    private String apiKey;

    public WeatherService(VisualCrossingClient visualCrossingClient,
                          ApiKeyService apiKeyService,
                          DtoMapperService mapper, WeatherRequestRepository weatherRequestRepository, WeatherAlertService weatherAlertService) {
        this.visualCrossingClient = visualCrossingClient;
        this.apiKeyService = apiKeyService;
        this.mapper = mapper;
        this.weatherRequestRepository = weatherRequestRepository;
        this.weatherAlertService = weatherAlertService;
    }

    private void saveRequest(String city, RequestType requestType, String apiKey) {
        WeatherRequest request = new WeatherRequest();
        request.setCity(city);
        request.setRequestType(requestType);
        request.setRequestTime(LocalDateTime.now());
        request.setApiKey(apiKey);
        weatherRequestRepository.save(request);
        log.debug("Сохранён запрос {} для города {}, ключ: {}", requestType, city, apiKey);
    }

    //Общий метод для получения текущей погоды
    private WeatherResponse fetchWeatherFromApi(String location, String lang) {
        log.info("Вызов API для получения погоды по локации {}", location);
        VisualCrossingResponse response = visualCrossingClient.getCurrentWeather(location, lang);
        return mapper.toWeatherResponse(response, location, lang);
    }

    // Вспомогательный метод для получения ключевых слов фильтра
    private List<String> getFilterKeywords(String filterCondition, Map<String, List<String>> synonyms) {
        String normalizedCondition = filterCondition.toLowerCase().trim();
        if (synonyms.containsKey(normalizedCondition)) {
            return synonyms.get(normalizedCondition);
        }

        for (Map.Entry<String, List<String>> entry : synonyms.entrySet()) {
            if (entry.getValue().contains(normalizedCondition)) {
                return entry.getValue();
            }
        }
        return List.of(normalizedCondition);
    }

    // Вспомогательный метод для проверки соответствия условий
    private boolean matchesCondition(String conditions, List<String> keywords) {
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }

        String conditionsLower = conditions.toLowerCase();

        for (String keyword : keywords) {
            if (conditionsLower.contains(keyword)) {
                log.debug("Совпадение: '{}' содержит '{}'", conditions, keyword);
                return true;
            }
        }
        return false;
    }

    //Проверка погодных условий для планировщика
    public String checkWeatherConditions(String city, int days, String lang) {
        ForecastResponse forecast = getForecast(city, days, apiKey, lang);
        ForecastResponse.DailyForecast today = forecast.getDaily().getFirst();
        return weatherAlertService.checkAlert(today.getTempMax(), today.getTempMin(),
                today.getWindSpeed(), today.getConditions());
    }

    //Текущая погода по городу (доступ free+)
    public WeatherResponse getCurrentWeatherByCity(String city, String apiKey, String lang) {
        apiKeyService.validateAndGetLevel(apiKey);
        log.info("Запрос текущей погоды для города {}", city);
        saveRequest(city, RequestType.CURRENT, apiKey);
        return fetchWeatherFromApi(city, lang);
    }

    //Текущая погода по координатам (доступ free+)
    public WeatherResponse getCurrentWeatherByCoordinates(Double lat, Double lon, String apiKey, String lang) {
        apiKeyService.validateAndGetLevel(apiKey);
        String location = lat + "," + lon;
        log.info("Запрос текущей погоды по координатам {}", location);
        saveRequest(location, RequestType.CURRENT, apiKey);
        return fetchWeatherFromApi(location, lang);
    }

    // фильтрация (доступ premium)
    public ForecastResponse getForecastWithFilter(String city, int days, String apiKey, String lang, String filterCondition) {
        apiKeyService.validateAndGetLevel(apiKey);
        ForecastResponse forecast = getForecast(city, days, apiKey, lang);
        if (filterCondition == null || filterCondition.trim().isEmpty()) {
            log.info("Фильтр не был указан, полный прогноз погоды для города {}", city);
            return forecast;
        }
        Map<String, List<String>> conditionSynonyms = new HashMap<>();
        conditionSynonyms.put("дождь", List.of("дождь", "rain", "drizzle", "ливень", "shower"));
        conditionSynonyms.put("солнце", List.of("солнечно", "ясно", "sun", "clear", "sunny", "fair"));
        conditionSynonyms.put("облачно", List.of("облачно", "пасмурно", "cloud", "overcast", "cloudy"));
        conditionSynonyms.put("снег", List.of("снег", "snow", "sleet", "метель", "blizzard"));
        conditionSynonyms.put("ветер", List.of("ветер", "wind", "windy", "шторм", "storm"));
        conditionSynonyms.put("гроза", List.of("гроза", "thunderstorm", "thunder", "lightning"));
        conditionSynonyms.put("туман", List.of("туман", "fog", "mist", "haze"));

        List<String> keywords = getFilterKeywords(filterCondition, conditionSynonyms);

        List<ForecastResponse.DailyForecast> filtered = new ArrayList<>();
        for (ForecastResponse.DailyForecast day : forecast.getDaily()) {
            if (matchesCondition(day.getConditions(), keywords)) {
                filtered.add(day);
            }
        }

        ForecastResponse filteredResponse = ForecastResponse.builder()
                .location(forecast.getLocation())
                .daily(filtered)
                .build();

        log.info("Фильтрация по условию '{}': из {} дней оставлено {}",
                filterCondition, forecast.getDaily().size(), filtered.size());
        return filteredResponse;
    }

    //Прогноз на N дней (доступ basic+)
    public ForecastResponse getForecast(String city, int days, String apiKey, String lang) {
        apiKeyService.validateAndGetLevel(apiKey);
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
            }
            log.info("Возвращено {} дней прогноза", dailyList.size());
        }
        saveRequest(city, RequestType.FILTERED_FORECAST, apiKey);
        return mapper.toForecastResponse(response, dailyList);
    }

    //Исторические данные за период (доступ до 7 дней назад basic, доступ до 8 месяцев периода - premium)
    public HistoricalResponse getHistoricalData(String city, String startDate, String endDate, String apiKey, String lang) {
        SubscriptionLevel level = apiKeyService.validateAndGetLevel(apiKey);
        LocalDate requestedDate = LocalDate.parse(startDate);
        LocalDate today = LocalDate.now();
        if (requestedDate.isAfter(today)) {
            throw new RuntimeException("Нельзя запрашивать историю для будущих дат");
        }

        if (level == SubscriptionLevel.BASIC) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
            long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(start, today);
            if (daysBetween > 7) {
                throw new RuntimeException("BASIC подписка позволяет запрашивать историю не более чем на 7 дней. Повысьте уровень подписки");
            }
            if (daysAgo > 7) {
                throw new RuntimeException("BASIC подписка позволяет запрашивать историю не ранее чем 7 дней назад. Повысьте уровень подписки");
            }
        }
        log.info("Запрос истории погоды для города {} с {} по {}", city, startDate, endDate);
        VisualCrossingResponse response = visualCrossingClient.getHistoricalData(city, startDate, endDate, lang);
        List<HistoricalResponse.DailyHistory> historyList = new ArrayList<>();
        if (response.getDays() != null) {
            for (Day day : response.getDays()) {
                historyList.add(mapper.toDailyHistory(day));
            }
        }
        saveRequest(city, RequestType.HISTORY, apiKey);
        return mapper.toHistoricalResponse(response, startDate, endDate, historyList);
    }

    //Погода в конкретное время (доступ premium)
    public WeatherResponse getWeatherAtTime(String city, String dateTime, String apiKey, String lang) {
        apiKeyService.validateAndGetLevel(apiKey);
        log.info("Запрос погоды для города {} на время {}", city, dateTime);
        VisualCrossingResponse response = visualCrossingClient.getWeatherAtTime(city, dateTime, lang);
        saveRequest(city, RequestType.AT_TIME, apiKey);
        return mapper.toWeatherResponse(response, city, dateTime);
    }

    //Почасовой прогноз погоды (доступ premium)
    public HourlyForecastResponse getHourlyForecast(String city, String date, String apiKey, String lang) {
        apiKeyService.validateAndGetLevel(apiKey);
        log.info("Запрос почасового прогноза для города {} на {}", city, date);
        VisualCrossingResponse response = visualCrossingClient.getHourlyForecast(city, date, lang);
        List<HourlyForecastResponse.HourlyData> hourlyList = new ArrayList<>();
        if (response.getDays() != null && !response.getDays().isEmpty()) {
            Day day = response.getDays().getFirst();
            if (day.getHours() != null) {
                for (Hour hour : day.getHours()) {
                    hourlyList.add(mapper.toHourlyData(hour));
                }
            }
        }
        saveRequest(city, RequestType.FORECAST, apiKey);
        return mapper.toHourlyForecastResponse(response, date, hourlyList);
    }
}