package ru.urfu.webapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.urfu.webapplication.model.PaymentStatus;
import ru.urfu.webapplication.model.SubscriptionLevel;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private String paymentId;
    private String apiKey;
    private SubscriptionLevel level;
    private Integer amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private String message;
}