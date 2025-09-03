package com.sierrarowerra.model.dto.bike;

import com.sierrarowerra.model.enums.BikeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BikeStatusUpdateRequestDto {
    @NotNull(message = "Status cannot be null")
    private BikeStatus status;
}
