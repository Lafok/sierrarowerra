package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.BikeRepository;
import com.sierrarowerra.domain.BookingRepository;
import com.sierrarowerra.domain.TariffRepository;
import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.BikeStatus;
import com.sierrarowerra.model.Tariff;
import com.sierrarowerra.model.dto.BikeRequestDto;
import com.sierrarowerra.model.dto.BikeStatusUpdateRequestDto;
import com.sierrarowerra.services.BikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BikeServiceImpl implements BikeService {

    private final BikeRepository bikeRepository;
    private final BookingRepository bookingRepository;
    private final TariffRepository tariffRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Bike> findAll(Pageable pageable) {
        return bikeRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bike> findById(Long id) {
        return bikeRepository.findById(id);
    }

    @Override
    @Transactional
    public Bike createBike(BikeRequestDto bikeRequest) {
        Tariff tariff = tariffRepository.findById(bikeRequest.getTariffId())
                .orElseThrow(() -> new IllegalArgumentException("Tariff not found with id: " + bikeRequest.getTariffId()));

        Bike newBike = new Bike();
        newBike.setName(bikeRequest.getName());
        newBike.setType(bikeRequest.getType());
        newBike.setStatus(BikeStatus.AVAILABLE);
        newBike.setTariff(tariff);
        return bikeRepository.save(newBike);
    }

    @Override
    @Transactional
    public Optional<Bike> updateBike(Long id, BikeRequestDto bikeRequest) {
        Tariff tariff = tariffRepository.findById(bikeRequest.getTariffId())
                .orElseThrow(() -> new IllegalArgumentException("Tariff not found with id: " + bikeRequest.getTariffId()));

        return bikeRepository.findById(id)
                .map(bike -> {
                    bike.setName(bikeRequest.getName());
                    bike.setType(bikeRequest.getType());
                    bike.setTariff(tariff);
                    return bikeRepository.save(bike);
                });
    }

    @Override
    @Transactional
    public Optional<Bike> updateBikeStatus(Long id, BikeStatusUpdateRequestDto statusRequest) {
        return bikeRepository.findById(id)
                .map(bike -> {
                    bike.setStatus(statusRequest.getStatus());
                    return bikeRepository.save(bike);
                });
    }

    @Override
    @Transactional
    public void deleteBike(Long id) {
        if (bookingRepository.existsByBikeId(id)) {
            throw new IllegalStateException("Cannot delete a bike with associated bookings.");
        }
        bikeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bike> findAvailableBikes(LocalDate startDate, LocalDate endDate) {
        List<Long> bookedBikeIds = bookingRepository.findBookedBikeIds(startDate, endDate);

        if (bookedBikeIds.isEmpty()) {
            return bikeRepository.findAll();
        } else {
            return bikeRepository.findByIdNotIn(bookedBikeIds);
        }
    }
}
