package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.BikeRepository;
import com.sierrarowerra.domain.BookingRepository;
import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.dto.BookingRequestDto;
import com.sierrarowerra.model.dto.BookingResponseDto;
import com.sierrarowerra.services.BookingService;
import com.sierrarowerra.services.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BikeRepository bikeRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public Booking createBooking(BookingRequestDto request) {
        Bike bike = bikeRepository.findById(request.getBikeId())
                .orElseThrow(() -> new IllegalArgumentException("Bike not found with id: " + request.getBikeId()));

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                request.getBikeId(),
                request.getStartDate(),
                request.getEndDate()
        );

        if (!overlappingBookings.isEmpty()) {
            throw new IllegalStateException("Bike is already booked for the selected dates.");
        }

        Booking newBooking = new Booking();
        newBooking.setBike(bike);
        newBooking.setCustomerName(request.getCustomerName());
        newBooking.setBookingStartDate(request.getStartDate());
        newBooking.setBookingEndDate(request.getEndDate());

        return bookingRepository.save(newBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> findAll() {
        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }
}
