package com.sierrarowerra.services;

import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.dto.BookingRequestDto;
import com.sierrarowerra.model.dto.BookingResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

public interface BookingService {
    Booking createBooking(BookingRequestDto bookingRequest, Long userId);

    Page<BookingResponseDto> findAll(Long userId, Set<String> roles, Pageable pageable);

    Optional<BookingResponseDto> findBookingById(Long bookingId, Long userId, Set<String> roles);

    void deleteBooking(Long bookingId, Long userId, Set<String> roles);

    Page<BookingResponseDto> getBookingHistory(Long userId, Set<String> roles, Pageable pageable);
}
