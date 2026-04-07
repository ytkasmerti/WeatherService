package ru.urfu.webapplication.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather_requests")
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

    // Конструктор
    public WeatherRequest() {}

    public WeatherRequest(String city, String requestType, LocalDateTime requestTime) {
        this.city = city;
        this.requestType = requestType;
        this.requestTime = requestTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getForecastDays() { return forecastDays; }
    public void setForecastDays(Integer forecastDays) { this.forecastDays = forecastDays; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }
}