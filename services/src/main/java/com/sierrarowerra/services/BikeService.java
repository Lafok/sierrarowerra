package com.sierrarowerra.services;

import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.dto.BikeRequestDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BikeService {
    List<Bike> findAll();

    Bike createBike(BikeRequestDto bikeRequest);

    Optional<Bike> updateBike(Long id, BikeRequestDto bikeRequest);

    void deleteBike(Long id);

    List<Bike> findAvailableBikes(LocalDate startDate, LocalDate endDate);
}
