package ru.urfu.webapplication.dto;

import lombok.Builder;
import lombok.Data;
import ru.urfu.webapplication.model.PaymentStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentInfoDto {
    private String paymentId;
    private String level;
    private Integer amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private String message;
}