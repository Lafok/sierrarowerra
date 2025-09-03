package com.sierrarowerra.model.dto.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingExtensionRequestDto {

    @NotNull(message = "New end date cannot be null")
    @Future(message = "New end date must be in the future")
    @Schema(example = "2025-12-31")
    private LocalDate newEndDate;
}
