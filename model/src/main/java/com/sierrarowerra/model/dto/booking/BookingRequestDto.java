package com.sierrarowerra.model.dto.booking;

import com.sierrarowerra.model.validation.ValidBookingDates;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(example = "2025-08-29")
    private LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @Future(message = "End date must be in the future")
    @Schema(example = "2025-08-30")
    private LocalDate endDate;
}
