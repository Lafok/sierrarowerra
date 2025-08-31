package com.sierrarowerra.services;

import com.sierrarowerra.model.dto.TariffDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TariffService {
    Page<TariffDto> findAll(Pageable pageable);

    Optional<TariffDto> findById(Long id);

    TariffDto createTariff(TariffDto tariffDto);

    Optional<TariffDto> updateTariff(Long id, TariffDto tariffDto);

    void deleteTariff(Long id);
}
