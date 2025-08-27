package com.sierrarowerra.services;

import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.dto.BikeRequestDto;
import com.sierrarowerra.model.dto.BikeStatusUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BikeService {
    Page<Bike> findAll(Pageable pageable);

    Optional<Bike> findById(Long id);

    Bike createBike(BikeRequestDto bikeRequest);

    Optional<Bike> updateBike(Long id, BikeRequestDto bikeRequest);

    Optional<Bike> updateBikeStatus(Long id, BikeStatusUpdateRequestDto statusRequest);

    void deleteBike(Long id);

    List<Bike> findAvailableBikes(LocalDate startDate, LocalDate endDate);
}
