package com.sierrarowerra.model.dto.payment;

import com.sierrarowerra.model.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDto {
    private Long id;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
}
