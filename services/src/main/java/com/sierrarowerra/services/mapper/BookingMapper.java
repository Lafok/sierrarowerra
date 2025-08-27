package com.sierrarowerra.services.mapper;

import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.dto.BikeDto;
import com.sierrarowerra.model.dto.BookingResponseDto;
import com.sierrarowerra.model.dto.UserDto;
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

        if (booking.getUser() != null) {
            UserDto userDto = new UserDto();
            userDto.setId(booking.getUser().getId());
            userDto.setUsername(booking.getUser().getUsername());
            userDto.setEmail(booking.getUser().getEmail());
            dto.setUser(userDto);
        }

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
