package com.sierrarowerra.services.bike;

import com.sierrarowerra.model.dto.bike.BikeRequestDto;
import com.sierrarowerra.model.dto.bike.BikeResponseDto;
import com.sierrarowerra.model.dto.bike.BikeStatusUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BikeService {
    Page<BikeResponseDto> findAll(Pageable pageable);

    Optional<BikeResponseDto> findById(Long id);

    BikeResponseDto createBike(BikeRequestDto bikeRequest);

    Optional<BikeResponseDto> updateBike(Long id, BikeRequestDto bikeRequest);

    Optional<BikeResponseDto> updateBikeStatus(Long id, BikeStatusUpdateRequestDto statusRequest);

    void deleteBike(Long id);

    List<BikeResponseDto> findAvailableBikes(LocalDate startDate, LocalDate endDate);

    Optional<BikeResponseDto> addImage(Long id, byte[] content, String originalFilename);

    Optional<BikeResponseDto> setPrimaryImage(Long bikeId, String imageUrl);

    Optional<BikeResponseDto> deleteImage(Long bikeId, String imageUrl);
}
