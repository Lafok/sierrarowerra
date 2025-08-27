package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.BikeRepository;
import com.sierrarowerra.domain.BookingRepository;
import com.sierrarowerra.domain.UserRepository;
import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.BikeStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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

        // Check if the bike is available for booking
        if (bike.getStatus() != BikeStatus.AVAILABLE) {
            throw new IllegalStateException("Bike is not available for booking. Current status: " + bike.getStatus());
        }

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
    public Page<BookingResponseDto> findAll(Long userId, Set<String> roles, Pageable pageable) {
        logger.info("Checking bookings for userId: {}. Roles: {}. Pageable: {}", userId, roles, pageable);

        boolean isUserAdmin = roles.stream().anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));

        Page<Booking> bookingsPage;
        if (isUserAdmin) {
            logger.warn("User is ADMIN. Fetching all bookings.");
            bookingsPage = bookingRepository.findAll(pageable);
        } else {
            logger.info("User is a regular user. Fetching bookings for userId: {}.", userId);
            bookingsPage = bookingRepository.findByUserId(userId, pageable);
        }

        return bookingsPage.map(bookingMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteBooking(Long bookingId, Long currentUserId, Set<String> roles) {
        logger.info("Attempting to delete booking {} by user {}", bookingId, currentUserId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        boolean isUserAdmin = roles.stream().anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));
        boolean isOwner = Objects.equals(booking.getUser().getId(), currentUserId);

        if (isUserAdmin || isOwner) {
            bookingRepository.delete(booking);
            logger.info("Booking {} deleted successfully by user {}", bookingId, currentUserId);
        } else {
            logger.warn("User {} is not authorized to delete booking {}", currentUserId, bookingId);
            throw new AccessDeniedException("You are not authorized to delete this booking.");
        }
    }
}
