package com.sierrarowerra.model.dto.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingExtensionRequestDto {

    @NotNull(message = "New end date cannot be null")
    @Future(message = "New end date must be in the future")
    @JsonFormat(pattern = "dd/MM/yyyy")
    @Schema(example = "31/12/2025")
    private LocalDate newEndDate;
}
