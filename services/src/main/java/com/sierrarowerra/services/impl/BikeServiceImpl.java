package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.BikeRepository;
import com.sierrarowerra.domain.BookingRepository;
import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.dto.BikeRequestDto;
import com.sierrarowerra.services.BikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BikeServiceImpl implements BikeService {

    private final BikeRepository bikeRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Bike> findAll() {
        return bikeRepository.findAll();
    }

    @Override
    @Transactional
    public Bike createBike(BikeRequestDto bikeRequest) {
        Bike newBike = new Bike();
        newBike.setName(bikeRequest.getName());
        newBike.setType(bikeRequest.getType());
        newBike.setAvailable(true); // By default, a new bike is available
        return bikeRepository.save(newBike);
    }

    @Override
    @Transactional
    public Optional<Bike> updateBike(Long id, BikeRequestDto bikeRequest) {
        return bikeRepository.findById(id)
                .map(bike -> {
                    bike.setName(bikeRequest.getName());
                    bike.setType(bikeRequest.getType());
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
}
