package ru.urfu.webapplication.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.urfu.webapplication.entity.WeatherRequest;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WeatherRequestRepository extends JpaRepository<WeatherRequest, Long> {

    // Поиск по городу
    Page<WeatherRequest> findByCity(String city, Pageable pageable);

    // Поиск всех запросов
    Page<WeatherRequest> findAll(Pageable pageable);

    List<WeatherRequest> findTop10ByOrderByRequestTimeDesc();

    @Query("SELECT COUNT(wr) FROM WeatherRequest wr WHERE wr.apiKey = :apiKey AND wr.requestTime >= :since")
    long countRequestsByKeyInLast24Hours(@Param("apiKey") String apiKey, @Param("since") LocalDateTime since);
}