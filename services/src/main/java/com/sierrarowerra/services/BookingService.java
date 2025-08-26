package com.sierrarowerra.services;

import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.dto.BookingRequestDto;
import com.sierrarowerra.model.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    Booking createBooking(BookingRequestDto bookingRequest);

    List<BookingResponseDto> findAll();
}
