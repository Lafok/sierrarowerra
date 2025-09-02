package com.sierrarowerra.controller;

import com.sierrarowerra.model.dto.BikeRequestDto;
import com.sierrarowerra.model.dto.BikeResponseDto;
import com.sierrarowerra.model.dto.BikeStatusUpdateRequestDto;
import com.sierrarowerra.services.BikeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bikes")
@RequiredArgsConstructor
public class BikeController {

    private final BikeService bikeService;

    @Operation(summary = "Get a paginated list of all bikes")
    @GetMapping
    public Page<BikeResponseDto> getAllBikes(@ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return bikeService.findAll(pageable);
    }

    @Operation(summary = "Get a specific bike by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<BikeResponseDto> getBikeById(@PathVariable Long id) {
        return bikeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Find available bikes for a given date range")
    @GetMapping("/available")
    public List<BikeResponseDto> getAvailableBikes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return bikeService.findAvailableBikes(startDate, endDate);
    }

    @Operation(summary = "Create a new bike (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BikeResponseDto> createBike(@Valid @RequestBody BikeRequestDto bikeRequest) {
        BikeResponseDto newBike = bikeService.createBike(bikeRequest);
        return new ResponseEntity<>(newBike, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing bike (Admin only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BikeResponseDto> updateBike(@PathVariable Long id, @Valid @RequestBody BikeRequestDto bikeRequest) {
        return bikeService.updateBike(id, bikeRequest)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update the status of a bike (Admin only)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BikeResponseDto> updateBikeStatus(@PathVariable Long id, @Valid @RequestBody BikeStatusUpdateRequestDto statusRequest) {
        return bikeService.updateBikeStatus(id, statusRequest)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Upload an image for a bike (Admin only)")
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BikeResponseDto> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return bikeService.addImage(id, file)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Set a primary image for a bike (Admin only)")
    @PatchMapping("/{id}/images/primary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BikeResponseDto> setPrimaryImage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String imageUrl = body.get("imageUrl");
        if (imageUrl == null) {
            return ResponseEntity.badRequest().build();
        }
        return bikeService.setPrimaryImage(id, imageUrl)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete an image from a bike (Admin only)")
    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BikeResponseDto> deleteImage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String imageUrl = body.get("imageUrl");
        if (imageUrl == null) {
            return ResponseEntity.badRequest().build();
        }
        return bikeService.deleteImage(id, imageUrl)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a bike (Admin only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBike(@PathVariable Long id) {
        bikeService.deleteBike(id);
        return ResponseEntity.noContent().build();
    }
}
