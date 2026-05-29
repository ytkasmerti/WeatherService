package ru.urfu.webapplication.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather_alerts")
@Data
public class WeatherAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private String message;
    private String alertDate;
    private LocalDateTime createdAt;
}