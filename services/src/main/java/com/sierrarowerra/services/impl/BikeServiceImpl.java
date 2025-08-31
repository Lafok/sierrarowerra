package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.BikeRepository;
import com.sierrarowerra.domain.BookingRepository;
import com.sierrarowerra.domain.TariffRepository;
import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.BikeStatus;
import com.sierrarowerra.model.Tariff;
import com.sierrarowerra.model.dto.BikeRequestDto;
import com.sierrarowerra.model.dto.BikeResponseDto;
import com.sierrarowerra.model.dto.BikeStatusUpdateRequestDto;
import com.sierrarowerra.services.BikeService;
import com.sierrarowerra.services.FileStorageService;
import com.sierrarowerra.services.mapper.TariffMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BikeServiceImpl implements BikeService {

    private final BikeRepository bikeRepository;
    private final BookingRepository bookingRepository;
    private final TariffRepository tariffRepository;
    private final FileStorageService fileStorageService;
    private final TariffMapper tariffMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<BikeResponseDto> findAll(Pageable pageable) {
        return bikeRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BikeResponseDto> findById(Long id) {
        return bikeRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional
    public BikeResponseDto createBike(BikeRequestDto bikeRequest) {
        Tariff tariff = tariffRepository.findById(bikeRequest.getTariffId())
                .orElseThrow(() -> new IllegalArgumentException("Tariff not found with id: " + bikeRequest.getTariffId()));

        Bike newBike = new Bike();
        newBike.setName(bikeRequest.getName());
        newBike.setType(bikeRequest.getType());
        newBike.setStatus(BikeStatus.AVAILABLE);
        newBike.setTariff(tariff);

        Bike savedBike = bikeRepository.save(newBike);
        return convertToDto(savedBike);
    }

    @Override
    @Transactional
    public Optional<BikeResponseDto> updateBike(Long id, BikeRequestDto bikeRequest) {
        Tariff tariff = tariffRepository.findById(bikeRequest.getTariffId())
                .orElseThrow(() -> new IllegalArgumentException("Tariff not found with id: " + bikeRequest.getTariffId()));

        return bikeRepository.findById(id)
                .map(bike -> {
                    bike.setName(bikeRequest.getName());
                    bike.setType(bikeRequest.getType());
                    bike.setTariff(tariff);
                    Bike savedBike = bikeRepository.save(bike);
                    return convertToDto(savedBike);
                });
    }

    @Override
    @Transactional
    public Optional<BikeResponseDto> updateBikeStatus(Long id, BikeStatusUpdateRequestDto statusRequest) {
        return bikeRepository.findById(id)
                .map(bike -> {
                    bike.setStatus(statusRequest.getStatus());
                    Bike savedBike = bikeRepository.save(bike);
                    return convertToDto(savedBike);
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
    public List<BikeResponseDto> findAvailableBikes(LocalDate startDate, LocalDate endDate) {
        List<Long> bookedBikeIds = bookingRepository.findBookedBikeIds(startDate, endDate);
        List<Bike> availableBikes;

        if (bookedBikeIds.isEmpty()) {
            availableBikes = bikeRepository.findAll();
        } else {
            availableBikes = bikeRepository.findByIdNotIn(bookedBikeIds);
        }

        return availableBikes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<BikeResponseDto> addImage(Long id, MultipartFile file) {
        return bikeRepository.findById(id)
                .map(bike -> {
                    String fileName = fileStorageService.storeFile(file);
                    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/uploads/")
                            .path(fileName)
                            .toUriString();

                    bike.getImageUrls().add(fileDownloadUri);
                    Bike savedBike = bikeRepository.save(bike);
                    return convertToDto(savedBike);
                });
    }

    private BikeResponseDto convertToDto(Bike bike) {
        BikeResponseDto dto = new BikeResponseDto();
        dto.setId(bike.getId());
        dto.setName(bike.getName());
        dto.setType(bike.getType());
        dto.setStatus(bike.getStatus());

        if (bike.getTariff() != null) {
            dto.setTariff(tariffMapper.toDto(bike.getTariff()));
        }

        if (bike.getImageUrls() != null) {
            dto.setImageUrls(List.copyOf(bike.getImageUrls()));
        } else {
            dto.setImageUrls(Collections.emptyList());
        }

        return dto;
    }
}
