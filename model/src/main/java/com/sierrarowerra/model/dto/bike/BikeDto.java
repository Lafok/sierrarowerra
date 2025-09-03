package com.sierrarowerra.model.dto.bike;

import com.sierrarowerra.model.enums.BikeType;
import lombok.Data;

@Data
public class BikeDto {
    private Long id;
    private String name;
    private BikeType type;
}
