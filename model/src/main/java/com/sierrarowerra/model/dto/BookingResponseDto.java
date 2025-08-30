package com.sierrarowerra.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sierrarowerra.model.ArchivalReason;
import com.sierrarowerra.model.BookingStatus;
import com.sierrarowerra.model.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Fields with null values will not be included in the JSON output
public class BookingResponseDto {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private UserDto user;
    private BikeDto bike;

    // For active bookings
    private BookingStatus status;

    // For historical bookings
    private ArchivalReason reason;
    private PaymentStatus paymentStatus;

    // Common fields
    private BigDecimal amount;
}
