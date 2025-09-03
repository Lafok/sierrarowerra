package com.sierrarowerra.services.booking;

import com.sierrarowerra.domain.booking.BookingHistoryRepository;
import com.sierrarowerra.domain.booking.BookingRepository;
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

import java.time.LocalDateTime;
import java.util.List;

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

        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.PENDING_PAYMENT, LocalDateTime.now());

        if (expiredBookings.isEmpty()) {
            logger.info("No expired bookings to archive.");
            return;
        }

        for (Booking booking : expiredBookings) {
            logger.warn("Booking {} has expired due to non-payment. Archiving...", booking.getId());

            BookingHistory history = new BookingHistory(
                    null,
                    booking.getId(),
                    booking.getBike(),
                    booking.getUser(),
                    booking.getBookingStartDate(),
                    booking.getBookingEndDate(),
                    ArchivalReason.PAYMENT_EXPIRED,
                    booking.getCreatedAt() // Pass the original creation timestamp
            );
            bookingHistoryRepository.save(history);

            paymentRepository.findByBookingId(booking.getId()).ifPresent(payment -> {
                PaymentHistory paymentHistory = new PaymentHistory(
                        null,
                        booking.getId(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        PaymentStatus.FAILED
                );
                paymentHistoryRepository.save(paymentHistory);
                paymentRepository.delete(payment);
                logger.info("Archived and deleted payment {} for expired booking {}", payment.getId(), booking.getId());
            });

            bookingRepository.delete(booking);
        }

        logger.info("Finished archiving {} expired bookings.", expiredBookings.size());
    }
}
