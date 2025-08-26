package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.BikeRepository;
import com.sierrarowerra.model.Bike;
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
}
