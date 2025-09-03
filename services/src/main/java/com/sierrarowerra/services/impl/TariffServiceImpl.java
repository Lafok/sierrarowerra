package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.tariff.TariffRepository;
import com.sierrarowerra.domain.tariff.Tariff;
import com.sierrarowerra.model.dto.tariff.TariffDto;
import com.sierrarowerra.services.TariffService;
import com.sierrarowerra.services.mapper.TariffMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TariffServiceImpl implements TariffService {

    private final TariffRepository tariffRepository;
    private final TariffMapper tariffMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<TariffDto> findAll(Pageable pageable) {
        return tariffRepository.findAll(pageable)
                .map(tariffMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TariffDto> findById(Long id) {
        return tariffRepository.findById(id)
                .map(tariffMapper::toDto);
    }

    @Override
    @Transactional
    public TariffDto createTariff(TariffDto tariffDto) {
        Tariff tariff = tariffMapper.toEntity(tariffDto);
        tariff = tariffRepository.save(tariff);
        return tariffMapper.toDto(tariff);
    }

    @Override
    @Transactional
    public Optional<TariffDto> updateTariff(Long id, TariffDto tariffDto) {
        return tariffRepository.findById(id)
                .map(tariff -> {
                    tariffMapper.updateTariffFromDto(tariffDto, tariff);
                    tariff = tariffRepository.save(tariff);
                    return tariffMapper.toDto(tariff);
                });
    }

    @Override
    @Transactional
    public void deleteTariff(Long id) {
        // Note: In a real application, we should check if this tariff is being used by any bikes before deleting.
        tariffRepository.deleteById(id);
    }
}
