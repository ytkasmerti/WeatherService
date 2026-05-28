package ru.urfu.webapplication.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PaymentHistoryDto {
    private List<PaymentInfoDto> payments;
    private int totalCount;
}