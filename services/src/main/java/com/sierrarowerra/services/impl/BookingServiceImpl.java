package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.*;
import com.sierrarowerra.model.*;
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

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public Payment createBooking(BookingRequestDto request, Long userId) {
        logger.info("Creating booking for user {} and bike {}", userId, request.getBikeId());

        Bike bike = bikeRepository.findById(request.getBikeId())
                .orElseThrow(() -> new IllegalArgumentException("Bike not found with id: " + request.getBikeId()));

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

        // --- Payment Calculation Logic ---
        Tariff tariff = bike.getTariff();
        long duration;
        if (tariff.getType() == TariffType.DAILY) {
            duration = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
            if (duration == 0) duration = 1; // Minimum 1 day rental
        } else { // HOURLY
            duration = ChronoUnit.HOURS.between(request.getStartDate().atStartOfDay(), request.getEndDate().atStartOfDay());
            if (duration == 0) duration = 1; // Minimum 1 hour rental
        }

        BigDecimal totalAmount = tariff.getPrice().multiply(new BigDecimal(duration));
        logger.info("Calculated payment amount: {} for a duration of {} {}", totalAmount, duration, tariff.getType());

        // --- Create Booking and Payment ---
        Booking newBooking = new Booking();
        newBooking.setBike(bike);
        newBooking.setUser(user);
        newBooking.setBookingStartDate(request.getStartDate());
        newBooking.setBookingEndDate(request.getEndDate());
        Booking savedBooking = bookingRepository.save(newBooking);

        Payment newPayment = new Payment();
        newPayment.setBooking(savedBooking);
        newPayment.setAmount(totalAmount);
        newPayment.setStatus(PaymentStatus.PENDING);
        Payment savedPayment = paymentRepository.save(newPayment);

        logger.info("Successfully created booking {} and payment with PENDING status", savedBooking.getId());

        return savedPayment;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> findAll(Long userId, Set<String> roles, Pageable pageable) {
        logger.info("Checking active bookings for userId: {}. Roles: {}. Pageable: {}", userId, roles, pageable);
        boolean isUserAdmin = roles.stream().anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));

        Page<Booking> bookingsPage;
        if (isUserAdmin) {
            bookingsPage = bookingRepository.findAll(pageable);
        } else {
            bookingsPage = bookingRepository.findByUserId(userId, pageable);
        }
        return bookingsPage.map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookingResponseDto> findBookingById(Long bookingId, Long currentUserId, Set<String> roles) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    boolean isUserAdmin = roles.stream().anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));
                    boolean isOwner = Objects.equals(booking.getUser().getId(), currentUserId);

                    if (isUserAdmin || isOwner) {
                        return bookingMapper.toDto(booking);
                    } else {
                        throw new AccessDeniedException("You are not authorized to view this booking.");
                    }
                });
    }

    @Override
    @Transactional
    public void deleteBooking(Long bookingId, Long currentUserId, Set<String> roles) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        boolean isUserAdmin = roles.stream().anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));
        boolean isOwner = Objects.equals(booking.getUser().getId(), currentUserId);

        if (isUserAdmin || isOwner) {
            bookingRepository.delete(booking);
        } else {
            throw new AccessDeniedException("You are not authorized to delete this booking.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> getBookingHistory(Long userId, Set<String> roles, Pageable pageable) {
        logger.info("Checking booking history for userId: {}. Roles: {}. Pageable: {}", userId, roles, pageable);
        boolean isUserAdmin = roles.stream().anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));

        Page<BookingHistory> historyPage;
        if (isUserAdmin) {
            historyPage = bookingHistoryRepository.findAll(pageable);
        } else {
            historyPage = bookingHistoryRepository.findByUserId(userId, pageable);
        }
        return historyPage.map(bookingMapper::toDto);
    }
}
