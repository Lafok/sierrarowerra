package com.sierrarowerra.services.mapper;

import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.dto.BikeDto;
import com.sierrarowerra.model.dto.BookingResponseDto;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponseDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStartDate(booking.getBookingStartDate());
        dto.setEndDate(booking.getBookingEndDate());
        dto.setCustomerName(booking.getCustomerName());

        if (booking.getBike() != null) {
            BikeDto bikeDto = new BikeDto();
            bikeDto.setId(booking.getBike().getId());
            bikeDto.setName(booking.getBike().getName());
            bikeDto.setType(booking.getBike().getType());
            dto.setBike(bikeDto);
        }

        return dto;
    }
}
