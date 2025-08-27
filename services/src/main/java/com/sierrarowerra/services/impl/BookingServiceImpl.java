package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.BikeRepository;
import com.sierrarowerra.domain.BookingRepository;
import com.sierrarowerra.domain.UserRepository;
import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.ERole;
import com.sierrarowerra.model.User;
import com.sierrarowerra.model.dto.BookingRequestDto;
import com.sierrarowerra.model.dto.BookingResponseDto;
import com.sierrarowerra.services.BookingService;
import com.sierrarowerra.services.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public Booking createBooking(BookingRequestDto request, Long userId) {
        Bike bike = bikeRepository.findById(request.getBikeId())
                .orElseThrow(() -> new IllegalArgumentException("Bike not found with id: " + request.getBikeId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

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
        newBooking.setUser(user);
        newBooking.setBookingStartDate(request.getStartDate());
        newBooking.setBookingEndDate(request.getEndDate());

        return bookingRepository.save(newBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> findAll(Long userId, Set<String> roles) {
        logger.info("Checking bookings for userId: {}. Roles: {}", userId, roles);

        boolean isUserAdmin = false;
        for (String role : roles) {
            if (role.equals(ERole.ROLE_ADMIN.name())) {
                isUserAdmin = true;
                break;
            }
        }

        List<Booking> bookings;
        if (isUserAdmin) {
            logger.warn("User is ADMIN. Fetching all bookings.");
            bookings = bookingRepository.findAll();
        } else {
            logger.info("User is a regular user. Fetching bookings for userId: {}.", userId);
            bookings = bookingRepository.findByUserId(userId);
        }

        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }
}
