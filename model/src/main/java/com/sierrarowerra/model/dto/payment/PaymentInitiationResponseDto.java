package com.sierrarowerra.model.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationResponseDto {
    private String clientSecret;
    private Long bookingId;
}
