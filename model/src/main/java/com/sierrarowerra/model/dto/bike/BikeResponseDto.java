package com.sierrarowerra.model.dto.bike;

import com.sierrarowerra.model.enums.BikeStatus;
import com.sierrarowerra.model.enums.BikeType;
import com.sierrarowerra.model.dto.image.ImageDto;
import com.sierrarowerra.model.dto.tariff.TariffDto;
import lombok.Data;

import java.util.List;

@Data
public class BikeResponseDto {
    private Long id;
    private String name;
    private BikeType type;
    private BikeStatus status;
    private TariffDto tariff;
    private List<ImageDto> images;
}
