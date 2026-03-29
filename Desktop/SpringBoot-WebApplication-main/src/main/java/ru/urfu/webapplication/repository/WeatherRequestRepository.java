package ru.urfu.webapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.webapplication.entity.WeatherRequest;
import java.util.List;

@Repository
public interface WeatherRequestRepository extends JpaRepository<WeatherRequest, Long> {

    // Найти все запросы погоды для конкретного города
    List<WeatherRequest> findByCity(String city);

    // Получить последние 10 запросов (сортируются по времени от новых к старым)
    List<WeatherRequest> findTop10ByOrderByRequestTimeDesc();
}