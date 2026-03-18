package com.sierrarowerra.services.booking;

import com.sierrarowerra.domain.booking.BookingHistoryRepository;
import com.sierrarowerra.domain.booking.BookingRepository;
import com.sierrarowerra.domain.payment.Payment;
import com.sierrarowerra.domain.payment.PaymentHistoryRepository;
import com.sierrarowerra.domain.payment.PaymentRepository;
import com.sierrarowerra.domain.booking.Booking;
import com.sierrarowerra.domain.booking.BookingHistory;
import com.sierrarowerra.domain.payment.PaymentHistory;
import com.sierrarowerra.model.enums.ArchivalReason;
import com.sierrarowerra.model.enums.BookingStatus;
import com.sierrarowerra.model.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(BookingCleanupService.class);

    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void cleanupExpiredBookings() {
        logger.info("Running scheduled job to archive expired bookings...");

        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.PENDING_PAYMENT, Instant.now());

        if (expiredBookings.isEmpty()) {
            logger.info("No expired bookings to archive.");
            return;
        }

        List<Long> expiredBookingIds = expiredBookings.stream().map(Booking::getId).toList();
        Map<Long, Payment> paymentMap = paymentRepository.findAllByBookingIdIn(expiredBookingIds).stream()
                .collect(java.util.stream.Collectors.toMap(p -> p.getBooking().getId(), p -> p));

        for (Booking booking : expiredBookings) {
            logger.warn("Booking {} has expired due to non-payment. Setting status to EXPIRED.", booking.getId());

            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);

            Payment payment = paymentMap.get(booking.getId());
            if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                logger.info("Marked payment {} as FAILED for expired booking {}", payment.getId(), booking.getId());
            }
        }

        logger.info("Finished archiving {} expired bookings.", expiredBookings.size());
    }
}
