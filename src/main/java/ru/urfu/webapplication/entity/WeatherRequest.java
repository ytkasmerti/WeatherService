package ru.urfu.webapplication.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Entity
@Table(name = "weather_requests")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Поля для хранения информации о запросе
    private String city;
    private String requestType; // "current", "forecast", "history"
    private Double latitude;
    private Double longitude;
    private Integer forecastDays;
    private String startDate;
    private String endDate;
    private LocalDateTime requestTime;

    @Column(name = "api_key")
    private String apiKey;

    public WeatherRequest(String city, String requestType, LocalDateTime requestTime) {
        this.city = city;
        this.requestType = requestType;
        this.requestTime = requestTime;
    }
}