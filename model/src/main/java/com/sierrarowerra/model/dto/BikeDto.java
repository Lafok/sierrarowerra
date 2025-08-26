package com.sierrarowerra.model.dto;

import com.sierrarowerra.model.BikeType;
import lombok.Data;

@Data
public class BikeDto {
    private Long id;
    private String name;
    private BikeType type;
}
