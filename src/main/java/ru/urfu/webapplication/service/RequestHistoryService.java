package ru.urfu.webapplication.service;

import org.springframework.stereotype.Service;
import ru.urfu.webapplication.entity.WeatherRequest;
import ru.urfu.webapplication.repository.WeatherRequestRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestHistoryService {

    private final WeatherRequestRepository requestRepository;

    public RequestHistoryService(WeatherRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    // Сохранить запрос о погоде по городу
    public void saveCityRequest(String city, String requestType) {
        WeatherRequest request = new WeatherRequest(city, requestType, LocalDateTime.now());
        requestRepository.save(request);
        System.out.println("Запрос сохранён: " + city + " - " + requestType);
    }

    // Получить историю запросов по городу
    public List<WeatherRequest> getHistoryByCity(String city) {
        return requestRepository.findByCity(city);
    }

    // Получить последние 10 запросов
    public List<WeatherRequest> getLast10Requests() {
        return requestRepository.findTop10ByOrderByRequestTimeDesc();
    }
}