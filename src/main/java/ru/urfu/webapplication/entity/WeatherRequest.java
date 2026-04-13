package ru.urfu.webapplication.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
}