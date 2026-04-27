package ru.urfu.webapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.urfu.webapplication.entity.ApiKey;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    // поиск ключа по значению и проверка его актичности
    Optional<ApiKey> findByKeyValueAndIsActiveTrue(String keyValue);
    // поиск ключа по email пользователя
    Optional<ApiKey> findByEmail(String email);

    @Query("SELECT COUNT(wr) FROM WeatherRequest wr WHERE wr.apiKey = :apiKey AND wr.requestTime >= :since")
    // подсчет количества запросов этого апи ключа за последние сутки
    long countRequestsByKeyInLast24Hours(@Param("apiKey") String apiKey, @Param("since") LocalDateTime since);
}