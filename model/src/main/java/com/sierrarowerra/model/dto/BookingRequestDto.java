package com.sierrarowerra.model.dto;

import com.sierrarowerra.model.validation.ValidBookingDates;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@ValidBookingDates
public class BookingRequestDto {

    @NotNull(message = "Bike ID cannot be null")
    private Long bikeId;

    @NotNull(message = "Start date cannot be null")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
}
