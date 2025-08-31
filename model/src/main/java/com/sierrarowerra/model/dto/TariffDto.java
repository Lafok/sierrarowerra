package com.sierrarowerra.model.dto;

import com.sierrarowerra.model.TariffType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TariffDto {

    private Long id;

    @NotBlank(message = "Tariff name cannot be empty")
    private String name;

    @NotNull(message = "Tariff type cannot be null")
    private TariffType type;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal price;

    private String description;
}
