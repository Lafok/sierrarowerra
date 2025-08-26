package com.sierrarowerra.controller;

import com.sierrarowerra.model.Bike;
import com.sierrarowerra.services.BikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bikes")
@RequiredArgsConstructor
public class BikeController {

    private final BikeService bikeService;

    @GetMapping
    public List<Bike> getAllBikes() {
        return bikeService.findAll();
    }
}
