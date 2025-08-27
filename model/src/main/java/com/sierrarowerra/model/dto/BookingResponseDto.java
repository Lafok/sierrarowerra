package com.sierrarowerra.model.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingResponseDto {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private UserDto user;
    private BikeDto bike;
}
