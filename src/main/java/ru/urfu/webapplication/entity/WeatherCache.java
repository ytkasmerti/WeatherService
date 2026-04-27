package ru.urfu.webapplication.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "weather_cache")
@Data

public class WeatherCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cache_key", unique = true, nullable = false, length = 300)
    private String cacheKey;

    @Column(name = "response_data", nullable = false, columnDefinition = "TEXT")
    private String responseData; // данные ответа от API

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // время, когда данные становятся не актуальными
}
