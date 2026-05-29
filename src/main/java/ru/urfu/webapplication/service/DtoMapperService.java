package ru.urfu.webapplication.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.urfu.webapplication.dto.*;
import ru.urfu.webapplication.dto.visualcrossingapi.Day;
import ru.urfu.webapplication.dto.visualcrossingapi.Hour;
import ru.urfu.webapplication.dto.visualcrossingapi.VisualCrossingResponse;
import ru.urfu.webapplication.entity.Payment;
import ru.urfu.webapplication.model.PaymentStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class DtoMapperService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String getStatusMessage(PaymentStatus status) {
        return switch (status) {
            case PaymentStatus.PENDING -> "Платёж ожидает подтверждения";
            case PaymentStatus.CONFIRMED -> "Платёж подтверждён";
            case PaymentStatus.FAILED -> "Платёж отклонён";
            case PaymentStatus.EXPIRED-> "Платёж просрочен";
        };
    }

    public WeatherResponse toWeatherResponse(VisualCrossingResponse response, String location, String lang) {
        log.debug("Маппинг текущей погоды для {}", location);
        return new WeatherResponse(
                response.getResolvedAddress(),
                response.getCurrentConditions().getTemp(),
                response.getCurrentConditions().getFeelsLike(),
                response.getCurrentConditions().getHumidity() != null ?
                        response.getCurrentConditions().getHumidity().intValue() : null,
                response.getCurrentConditions().getWindSpeed(),
                response.getCurrentConditions().getWindDirection(),
                response.getCurrentConditions().getPressure(),
                response.getCurrentConditions().getConditions(),
                LocalDateTime.now().format(formatter)
        );
    }

    public ForecastResponse toForecastResponse(VisualCrossingResponse response, List<ForecastResponse.DailyForecast> dailyList) {
        return new ForecastResponse(response.getResolvedAddress(), dailyList);
    }

    public ForecastResponse.DailyForecast toDailyForecast(Day day) {
        return new ForecastResponse.DailyForecast(
                day.getDatetime(),
                day.getTempMax(),
                day.getTempMin(),
                day.getTemp(),
                day.getFeelsLike(),
                day.getHumidity() != null ? day.getHumidity().intValue() : null,
                day.getWindSpeed(),
                day.getWindDirection(),
                day.getPressure(),
                day.getConditions(),
                day.getUvIndex(),
                day.getSunrise(),
                day.getSunset()
        );
    }

    public HistoricalResponse toHistoricalResponse(VisualCrossingResponse response, String startDate, String endDate, List<HistoricalResponse.DailyHistory> historyList) {
        return new HistoricalResponse(
                response.getResolvedAddress(),
                startDate,
                endDate,
                historyList
        );
    }

    public HistoricalResponse.DailyHistory toDailyHistory(Day day) {
        return new HistoricalResponse.DailyHistory(
                day.getDatetime(),
                day.getTempMax(),
                day.getTempMin(),
                day.getTemp(),
                day.getFeelsLike(),
                day.getHumidity() != null ? day.getHumidity().intValue() : null,
                day.getWindSpeed(),
                day.getWindDirection(),
                day.getPressure(),
                day.getConditions(),
                day.getUvIndex(),
                day.getSunrise(),
                day.getSunset()
        );
    }

    public HourlyForecastResponse toHourlyForecastResponse(VisualCrossingResponse response, String date, List<HourlyForecastResponse.HourlyData> hourlyList) {
        return HourlyForecastResponse.builder()
                .location(response.getResolvedAddress())
                .date(date)
                .hours(hourlyList)
                .build();
    }

    public HourlyForecastResponse.HourlyData toHourlyData(Hour hour) {
        return HourlyForecastResponse.HourlyData.builder()
                .time(hour.getDatetime())
                .temperature(hour.getTemp())
                .feelsLike(hour.getFeelsLike())
                .humidity(hour.getHumidity() != null ? hour.getHumidity().intValue() : null)
                .windSpeed(hour.getWindSpeed())
                .windDirection(hour.getWindDirection())
                .pressure(hour.getPressure())
                .conditions(hour.getConditions())
                .uvIndex(hour.getUvIndex())
                .build();
    }

    public PaymentDto toPaymentDto(Payment payment) {
        if (payment == null) return null;

        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .apiKey(payment.getApiKey())
                .level(payment.getLevel())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .message(getStatusMessage(payment.getStatus()))
                .build();
    }

    public PaymentInfoDto toPaymentInfoDto(Payment payment) {
        if (payment == null) return null;

        return PaymentInfoDto.builder()
                .paymentId(payment.getPaymentId())
                .level(payment.getLevel())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .confirmedAt(payment.getConfirmedAt())
                .message(getStatusMessage(payment.getStatus()))
                .build();
    }

    public PaymentHistoryDto toPaymentHistoryDto(String email, List<Payment> payments) {
        List<PaymentInfoDto> paymentInfoList = payments.stream()
                .map(this::toPaymentInfoDto)
                .toList();

        return PaymentHistoryDto.builder()
                .payments(paymentInfoList)
                .totalCount(paymentInfoList.size())
                .build();
    }

    public PaymentDto buildPaymentDto(Payment payment, String apiKey, String message) {
        if (payment == null) return null;

        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .apiKey(apiKey != null ? apiKey : payment.getApiKey())
                .level(payment.getLevel())
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .message(message)
                .build();
    }
}