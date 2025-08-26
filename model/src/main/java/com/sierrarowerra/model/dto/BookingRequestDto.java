package com.sierrarowerra.model.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequestDto {
    private Long bikeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String customerName;
}
