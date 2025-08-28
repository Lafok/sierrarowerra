package com.sierrarowerra.model.dto;

import com.sierrarowerra.model.BikeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BikeRequestDto {

    @NotBlank(message = "Bike name cannot be empty")
    private String name;

    @NotNull(message = "Bike type cannot be null")
    private BikeType type;

    @NotNull(message = "Tariff ID cannot be null")
    private Long tariffId;
}
