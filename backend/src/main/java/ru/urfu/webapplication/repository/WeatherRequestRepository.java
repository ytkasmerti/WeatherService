package ru.urfu.webapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.urfu.webapplication.entity.WeatherRequest;

import java.time.LocalDateTime;

@Repository
public interface WeatherRequestRepository extends JpaRepository<WeatherRequest, Long> {

    @Query("SELECT COUNT(wr) FROM WeatherRequest wr WHERE wr.apiKey = :apiKey AND wr.requestTime >= :since")
    long countRequestsByKeyInLast24Hours(@Param("apiKey") String apiKey, @Param("since") LocalDateTime since);
}