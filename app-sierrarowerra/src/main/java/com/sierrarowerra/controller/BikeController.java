package com.sierrarowerra.controller;

import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.dto.BikeRequestDto;
import com.sierrarowerra.services.BikeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bikes")
@RequiredArgsConstructor
public class BikeController {

    private final BikeService bikeService;

    @SecurityRequirements
    @GetMapping
    public List<Bike> getAllBikes() {
        return bikeService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Bike> createBike(@Valid @RequestBody BikeRequestDto bikeRequest) {
        Bike newBike = bikeService.createBike(bikeRequest);
        return new ResponseEntity<>(newBike, HttpStatus.CREATED);
    }
}
