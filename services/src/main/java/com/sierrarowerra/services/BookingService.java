package com.sierrarowerra.services;

import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.dto.BookingRequestDto;
import com.sierrarowerra.model.dto.BookingResponseDto;

import java.util.List;
import java.util.Set;

public interface BookingService {
    Booking createBooking(BookingRequestDto bookingRequest, Long userId);

    List<BookingResponseDto> findAll(Long userId, Set<String> roles);
}
