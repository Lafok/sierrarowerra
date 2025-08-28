package com.sierrarowerra.model.dto;

import com.sierrarowerra.model.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDto {
    private Long id;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
}
