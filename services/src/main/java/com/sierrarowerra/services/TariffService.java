package com.sierrarowerra.services;

import com.sierrarowerra.model.Tariff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TariffService {
    Page<Tariff> findAll(Pageable pageable);

    Optional<Tariff> findById(Long id);

    Tariff createTariff(Tariff tariff);

    Optional<Tariff> updateTariff(Long id, Tariff tariffDetails);

    void deleteTariff(Long id);
}
