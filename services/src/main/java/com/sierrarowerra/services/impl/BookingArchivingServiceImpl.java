package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.BookingHistoryRepository;
import com.sierrarowerra.domain.BookingRepository;
import com.sierrarowerra.domain.PaymentHistoryRepository;
import com.sierrarowerra.domain.PaymentRepository;
import com.sierrarowerra.model.*;
import com.sierrarowerra.services.BookingArchivingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingArchivingServiceImpl implements BookingArchivingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingArchivingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Override
    @Transactional
    public void archiveOldBookings() {
        logger.info("Starting booking archiving process for completed bookings...");
        List<Booking> bookingsToArchive = bookingRepository.findAllByBookingEndDateBefore(LocalDate.now());

        if (bookingsToArchive.isEmpty()) {
            logger.info("No completed bookings to archive.");
            return;
        }

        for (Booking booking : bookingsToArchive) {
            logger.info("Archiving completed booking {}", booking.getId());

            BookingHistory history = new BookingHistory(
                    null,
                    booking.getId(),
                    booking.getBike(),
                    booking.getUser(),
                    booking.getBookingStartDate(),
                    booking.getBookingEndDate(),
                    ArchivalReason.COMPLETED,
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
                logger.info("Archived and deleted payment {} for completed booking {}", payment.getId(), booking.getId());
            });

            bookingRepository.delete(booking);
        }

        logger.info("Successfully archived {} completed bookings.", bookingsToArchive.size());
    }
}
