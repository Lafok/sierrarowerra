package com.sierrarowerra.model.dto;

import com.sierrarowerra.model.BikeStatus;
import com.sierrarowerra.model.BikeType;
import lombok.Data;
import java.util.List;

@Data
public class BikeResponseDto {
    private Long id;
    private String name;
    private BikeType type;
    private BikeStatus status;
    private Long tariffId;
    private Double tariffPrice;
    private List<String> imageUrls;
}