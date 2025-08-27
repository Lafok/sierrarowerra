package com.sierrarowerra.services;

import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.dto.BikeRequestDto;

import java.util.List;

public interface BikeService {
    List<Bike> findAll();

    Bike createBike(BikeRequestDto bikeRequest);
}
