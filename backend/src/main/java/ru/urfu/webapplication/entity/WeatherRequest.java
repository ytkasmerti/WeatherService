package ru.urfu.webapplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.urfu.webapplication.model.RequestType;

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
    private String city;
    @Enumerated(EnumType.STRING)
    private RequestType requestType;
    private Double latitude;
    private Double longitude;
    private Integer forecastDays;
    private String startDate;
    private String endDate;
    private LocalDateTime requestTime;
    @Column(name = "api_key")
    private String apiKey;
}