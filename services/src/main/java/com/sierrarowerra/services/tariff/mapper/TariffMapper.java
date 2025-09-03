package com.sierrarowerra.services.tariff.mapper;

import com.sierrarowerra.domain.tariff.Tariff;
import com.sierrarowerra.model.dto.tariff.TariffDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TariffMapper {

    TariffMapper INSTANCE = Mappers.getMapper(TariffMapper.class);

    TariffDto toDto(Tariff tariff);

    Tariff toEntity(TariffDto tariffDto);

    void updateTariffFromDto(TariffDto tariffDto, @MappingTarget Tariff tariff);
}
