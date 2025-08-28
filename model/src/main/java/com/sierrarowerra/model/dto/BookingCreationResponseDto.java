package com.sierrarowerra.model.dto;

import lombok.Data;

@Data
public class BookingCreationResponseDto {
    private Long bookingId;
    private PaymentDto paymentDetails;
}
