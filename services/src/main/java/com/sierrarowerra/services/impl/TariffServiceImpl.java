package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.TariffRepository;
import com.sierrarowerra.model.Tariff;
import com.sierrarowerra.services.TariffService;
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

    @Override
    @Transactional(readOnly = true)
    public Page<Tariff> findAll(Pageable pageable) {
        return tariffRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tariff> findById(Long id) {
        return tariffRepository.findById(id);
    }

    @Override
    @Transactional
    public Tariff createTariff(Tariff tariff) {
        return tariffRepository.save(tariff);
    }

    @Override
    @Transactional
    public Optional<Tariff> updateTariff(Long id, Tariff tariffDetails) {
        return tariffRepository.findById(id)
                .map(tariff -> {
                    tariff.setName(tariffDetails.getName());
                    tariff.setType(tariffDetails.getType());
                    tariff.setPrice(tariffDetails.getPrice());
                    tariff.setDescription(tariffDetails.getDescription());
                    return tariffRepository.save(tariff);
                });
    }

    @Override
    @Transactional
    public void deleteTariff(Long id) {
        // Note: In a real application, we should check if this tariff is being used by any bikes before deleting.
        tariffRepository.deleteById(id);
    }
}
