package com.sierrarowerra.services;

import com.sierrarowerra.domain.BookingRepository;
import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.BookingStatus;
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

    /**
     * This task runs every 5 minutes to clean up expired bookings.
     * It finds bookings that are still in PENDING_PAYMENT status and whose expiration time has passed.
     * It then changes their status to EXPIRED.
     */
    @Scheduled(fixedRate = 300000) // 300,000 milliseconds = 5 minutes
    @Transactional
    public void cleanupExpiredBookings() {
        logger.info("Running scheduled job to clean up expired bookings...");

        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.PENDING_PAYMENT, LocalDateTime.now());

        if (expiredBookings.isEmpty()) {
            logger.info("No expired bookings found.");
            return;
        }

        for (Booking booking : expiredBookings) {
            logger.warn("Booking {} has expired. Changing status to EXPIRED.", booking.getId());
            booking.setStatus(BookingStatus.EXPIRED);
        }

        bookingRepository.saveAll(expiredBookings);
        logger.info("Finished cleaning up {} expired bookings.", expiredBookings.size());
    }
}
