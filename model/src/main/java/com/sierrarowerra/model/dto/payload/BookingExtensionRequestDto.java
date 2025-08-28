package com.sierrarowerra.model.dto.payload;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingExtensionRequestDto {

    @NotNull(message = "New end date cannot be null")
    @Future(message = "New end date must be in the future")
    private LocalDate newEndDate;
}
