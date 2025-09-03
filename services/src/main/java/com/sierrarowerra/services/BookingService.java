package com.sierrarowerra.services;

import com.sierrarowerra.model.dto.booking.BookingRequestDto;
import com.sierrarowerra.model.dto.booking.BookingResponseDto;
import com.sierrarowerra.model.dto.payment.PaymentInitiationResponseDto;
import com.sierrarowerra.model.dto.booking.BookingExtensionRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;
import java.util.List;

public interface BookingService {

    PaymentInitiationResponseDto initiateBooking(BookingRequestDto bookingRequest, Long userId);

    Page<BookingResponseDto> findAll(Long userId, Set<String> roles, Pageable pageable);

    Optional<BookingResponseDto> findBookingById(Long bookingId, Long userId, Set<String> roles);

    void deleteBooking(Long bookingId, Long userId, Set<String> roles);

    Page<BookingResponseDto> getBookingHistory(Long userId, Set<String> roles, Pageable pageable);

    PaymentInitiationResponseDto extendBooking(Long bookingId, BookingExtensionRequestDto extensionRequest, Long currentUserId, Set<String> roles);

    List<String> getBookedDatesForBike(Long bikeId);
}
