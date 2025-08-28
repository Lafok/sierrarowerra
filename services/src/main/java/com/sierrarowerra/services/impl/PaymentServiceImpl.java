package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.BookingRepository;
import com.sierrarowerra.domain.PaymentRepository;
import com.sierrarowerra.model.*;
import com.sierrarowerra.model.dto.PaymentDto;
import com.sierrarowerra.services.PaymentService;
import com.sierrarowerra.services.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public Payment processPaymentForBooking(Long bookingId, Long currentUserId, Set<String> roles) {
        logger.info("Processing payment for booking {} by user {}", bookingId, currentUserId);

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for booking id: " + bookingId));

        Booking booking = payment.getBooking();

        // Authorization check
        boolean isUserAdmin = roles.stream().anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));
        boolean isOwner = Objects.equals(booking.getUser().getId(), currentUserId);

        if (!isUserAdmin && !isOwner) {
            logger.warn("User {} is not authorized to process payment for booking {}", currentUserId, bookingId);
            throw new AccessDeniedException("You are not authorized to pay for this booking.");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("This booking has already been paid for.");
        }

        // Simulate payment processing
        payment.setStatus(PaymentStatus.COMPLETED);

        Bike bike = booking.getBike();
        bike.setStatus(BikeStatus.RENTED);

        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Payment for booking {} successfully COMPLETED. Bike {} status set to RENTED.", bookingId, bike.getId());

        return savedPayment;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> findPaymentsForCurrentUser(Long userId, Set<String> roles, Pageable pageable) {
        boolean isUserAdmin = roles.stream().anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));

        Page<Payment> paymentPage;
        if (isUserAdmin) {
            paymentPage = paymentRepository.findAll(pageable);
        } else {
            paymentPage = paymentRepository.findByBooking_User_Id(userId, pageable);
        }
        return paymentPage.map(paymentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> findPaymentsByUserId(Long userId, Pageable pageable) {
        return paymentRepository.findByBooking_User_Id(userId, pageable).map(paymentMapper::toDto);
    }
}
