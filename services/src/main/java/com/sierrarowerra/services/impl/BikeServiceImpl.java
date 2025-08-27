package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.BikeRepository;
import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.dto.BikeRequestDto;
import com.sierrarowerra.services.BikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BikeServiceImpl implements BikeService {

    private final BikeRepository bikeRepository;

    @Override
    public List<Bike> findAll() {
        return bikeRepository.findAll();
    }

    @Override
    public Bike createBike(BikeRequestDto bikeRequest) {
        Bike newBike = new Bike();
        newBike.setName(bikeRequest.getName());
        newBike.setType(bikeRequest.getType());
        newBike.setAvailable(true); // By default, a new bike is available
        return bikeRepository.save(newBike);
    }
}
