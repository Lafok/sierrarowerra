package com.sierrarowerra.services.booking.impl;

import com.sierrarowerra.domain.bike.Bike;
import com.sierrarowerra.domain.bike.BikeRepository;
import com.sierrarowerra.domain.booking.Booking;
import com.sierrarowerra.domain.booking.BookingHistory;
import com.sierrarowerra.domain.booking.BookingHistoryRepository;
import com.sierrarowerra.domain.booking.BookingRepository;
import com.sierrarowerra.domain.payment.Payment;
import com.sierrarowerra.domain.payment.PaymentHistory;
import com.sierrarowerra.domain.payment.PaymentHistoryRepository;
import com.sierrarowerra.domain.payment.PaymentRepository;
import com.sierrarowerra.domain.tariff.Tariff;
import com.sierrarowerra.domain.user.User;
import com.sierrarowerra.domain.user.UserRepository;
import com.sierrarowerra.model.dto.booking.BookingRequestDto;
import com.sierrarowerra.model.dto.booking.BookingResponseDto;
import com.sierrarowerra.model.dto.payment.PaymentInitiationResponseDto;
import com.sierrarowerra.model.dto.booking.BookingExtensionRequestDto;
import com.sierrarowerra.model.enums.*;
import com.sierrarowerra.services.booking.BookingService;
import com.sierrarowerra.services.booking.mapper.BookingMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final BookingMapper bookingMapper;

    private final String stripeSecretKey;

    public BookingServiceImpl(BookingRepository bookingRepository, BookingHistoryRepository bookingHistoryRepository,
                              BikeRepository bikeRepository, UserRepository userRepository, PaymentRepository paymentRepository,
                              PaymentHistoryRepository paymentHistoryRepository, BookingMapper bookingMapper,
                              @Value("${stripe.api.secret-key}") String stripeSecretKey) {
        this.bookingRepository = bookingRepository;
        this.bookingHistoryRepository = bookingHistoryRepository;
        this.bikeRepository = bikeRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.bookingMapper = bookingMapper;
        this.stripeSecretKey = stripeSecretKey;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    @Transactional
    public PaymentInitiationResponseDto initiateBooking(BookingRequestDto request, Long userId) {
        logger.info("Initiating booking for user {} and bike {}", userId, request.getBikeId());

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

        Tariff tariff = bike.getTariff();
        long duration;
        if (tariff.getType() == TariffType.DAILY) {
            duration = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
            if (duration == 0) duration = 1;
        } else {
            duration = ChronoUnit.HOURS.between(request.getStartDate().atStartOfDay(), request.getEndDate().atStartOfDay());
            if (duration == 0) duration = 1;
        }

        BigDecimal totalAmount = tariff.getPrice().multiply(new BigDecimal(duration));
        logger.info("Calculated payment amount: {} for a duration of {} {}", totalAmount, duration, tariff.getType());

        Booking newBooking = new Booking();
        newBooking.setBike(bike);
        newBooking.setUser(user);
        newBooking.setBookingStartDate(request.getStartDate());
        newBooking.setBookingEndDate(request.getEndDate());
        newBooking.setStatus(BookingStatus.PENDING_PAYMENT);
        newBooking.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        newBooking.setCreatedAt(ZonedDateTime.now(ZoneId.of("CET"))); // Set creation timestamp
        Booking savedBooking = bookingRepository.save(newBooking);

        Payment payment = new Payment();
        payment.setBooking(savedBooking);
        payment.setAmount(totalAmount);
        payment.setCurrency("eur");
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(totalAmount.multiply(new BigDecimal(100)).longValue())
                    .setCurrency("eur")
                    .putMetadata("bookingId", savedBooking.getId().toString())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            logger.info("Successfully created Stripe PaymentIntent {} for booking {}", paymentIntent.getId(), savedBooking.getId());
            return new PaymentInitiationResponseDto(paymentIntent.getClientSecret(), savedBooking.getId());

        } catch (StripeException e) {
            logger.error("Error creating Stripe PaymentIntent for booking {}", savedBooking.getId(), e);
            throw new RuntimeException("Error communicating with payment provider.", e);
        }
    }

    @Override
    @Transactional
    public void deleteBooking(Long bookingId, Long currentUserId, Set<String> roles) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        boolean isUserAdmin = roles.stream().anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));
        boolean isOwner = Objects.equals(booking.getUser().getId(), currentUserId);

        if (!isUserAdmin && !isOwner) {
            throw new AccessDeniedException("You are not authorized to cancel this booking.");
        }

        logger.info("User {} is cancelling booking {}. Archiving...", currentUserId, bookingId);

        BookingHistory history = new BookingHistory(
                null,
                booking.getId(),
                booking.getBike(),
                booking.getUser(),
                booking.getBookingStartDate(),
                booking.getBookingEndDate(),
                ArchivalReason.CANCELLED_BY_USER,
                booking.getCreatedAt() // Pass the original creation timestamp
        );
        bookingHistoryRepository.save(history);

        paymentRepository.findByBookingId(booking.getId()).ifPresent(payment -> {
            PaymentHistory paymentHistory = new PaymentHistory(
                    null,
                    booking.getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getStatus()
            );
            paymentHistoryRepository.save(paymentHistory);
            paymentRepository.delete(payment);
            logger.info("Archived and deleted payment {} for cancelled booking {} with status {}", payment.getId(), booking.getId(), payment.getStatus());
        });

        bookingRepository.delete(booking);
        logger.info("Successfully cancelled and archived booking {}", bookingId);
    }

    @Override
    @Transactional
    public PaymentInitiationResponseDto extendBooking(Long bookingId, BookingExtensionRequestDto extensionRequest, Long currentUserId, Set<String> roles) {
        logger.info("Initiating extension for booking {} for user {} with new end date {}", bookingId, currentUserId, extensionRequest.getNewEndDate());
        Booking originalBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));
        boolean isUserAdmin = roles.stream().anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));
        boolean isOwner = Objects.equals(originalBooking.getUser().getId(), currentUserId);
        if (!isUserAdmin && !isOwner) {
            throw new AccessDeniedException("You are not authorized to extend this booking.");
        }
        if (!extensionRequest.getNewEndDate().isAfter(originalBooking.getBookingEndDate())) {
            throw new IllegalStateException("New end date must be after the current end date.");
        }
        if (originalBooking.getBookingEndDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot extend a booking that has already ended.");
        }
        BookingRequestDto extensionDto = new BookingRequestDto();
        extensionDto.setBikeId(originalBooking.getBike().getId());
        extensionDto.setStartDate(originalBooking.getBookingEndDate());
        extensionDto.setEndDate(extensionRequest.getNewEndDate());
        logger.info("Calling initiateBooking for the extension period.");
        return initiateBooking(extensionDto, originalBooking.getUser().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> findAll(Long userId, Set<String> roles, Pageable pageable) {
        boolean isUserAdmin = roles.stream().anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));
        Page<Booking> bookingsPage = isUserAdmin ? bookingRepository.findAll(pageable) : bookingRepository.findByUserId(userId, pageable);
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
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> getBookingHistory(Long userId, Set<String> roles, Pageable pageable) {
        boolean isUserAdmin = roles.stream().anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));
        Page<BookingHistory> historyPage = isUserAdmin ? bookingHistoryRepository.findAll(pageable) : bookingHistoryRepository.findByUserId(userId, pageable);
        return historyPage.map(bookingMapper::toDto);
    }
    @Override
    @Transactional(readOnly = true)
    public List<String> getBookedDatesForBike(Long bikeId) {

        List<Booking> bookings = bookingRepository.findByBikeIdAndStatus(bikeId, BookingStatus.CONFIRMED);

        Set<LocalDate> bookedDates = new HashSet<>();

        for (Booking booking : bookings) {
            LocalDate startDate = booking.getBookingStartDate();
            LocalDate endDate = booking.getBookingEndDate();

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                bookedDates.add(date);
            }
        }

        return bookedDates.stream()
                .map(LocalDate::toString)
                .collect(Collectors.toList());
    }
}
