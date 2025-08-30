package com.sierrarowerra.services.mapper;

import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.BookingHistory;
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
        dto.setStatus(booking.getStatus()); // Add this line

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

    public BookingResponseDto toDto(BookingHistory bookingHistory) {
        if (bookingHistory == null) {
            return null;
        }

        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(bookingHistory.getId());
        dto.setStartDate(bookingHistory.getBookingStartDate());
        dto.setEndDate(bookingHistory.getBookingEndDate());

        if (bookingHistory.getUser() != null) {
            UserDto userDto = new UserDto();
            userDto.setId(bookingHistory.getUser().getId());
            userDto.setUsername(bookingHistory.getUser().getUsername());
            userDto.setEmail(bookingHistory.getUser().getEmail());
            dto.setUser(userDto);
        }

        if (bookingHistory.getBike() != null) {
            BikeDto bikeDto = new BikeDto();
            bikeDto.setId(bookingHistory.getBike().getId());
            bikeDto.setName(bookingHistory.getBike().getName());
            bikeDto.setType(bookingHistory.getBike().getType());
            dto.setBike(bikeDto);
        }

        return dto;
    }
}
