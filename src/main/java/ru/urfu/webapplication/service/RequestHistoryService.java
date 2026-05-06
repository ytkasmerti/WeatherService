//package ru.urfu.webapplication.service;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import ru.urfu.webapplication.entity.WeatherRequest;
//import ru.urfu.webapplication.repository.WeatherRequestRepository;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//public class RequestHistoryService {
//
//    private final WeatherRequestRepository requestRepository;
//
//    public RequestHistoryService(WeatherRequestRepository requestRepository) {
//        this.requestRepository = requestRepository;
//    }
//
//    // Сохранить запрос о погоде по городу
//    public void saveCityRequest(String city, String requestType) {
//        WeatherRequest request = new WeatherRequest(city, requestType, LocalDateTime.now());
//        requestRepository.save(request);
//        System.out.println("Запрос сохранён: " + city + " - " + requestType);
//    }
//
//    // Получить историю запросов по городу
//    public Page<WeatherRequest> getHistoryByCity(String city, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("requestTime").descending());
//        return requestRepository.findByCity(city, pageable);
//    }
//
//    // Получить последние 10 запросов
//    public List<WeatherRequest> getLast10Requests() {
//        return requestRepository.findTop10ByOrderByRequestTimeDesc();
//    }
//}