package com.sierrarowerra.model.dto.booking;

import com.sierrarowerra.model.dto.payment.PaymentDto;
import lombok.Data;

@Data
public class BookingCreationResponseDto {
    private Long bookingId;
    private PaymentDto paymentDetails;
}
