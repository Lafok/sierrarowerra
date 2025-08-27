package com.sierrarowerra.services.impl;

import com.sierrarowerra.domain.BookingHistoryRepository;
import com.sierrarowerra.domain.BookingRepository;
import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.BookingHistory;
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

    @Override
    @Transactional
    public void archiveOldBookings() {
        logger.info("Starting booking archiving process...");
        List<Booking> bookingsToArchive = bookingRepository.findAllByBookingEndDateBefore(LocalDate.now());

        if (bookingsToArchive.isEmpty()) {
            logger.info("No bookings to archive.");
            return;
        }

        for (Booking booking : bookingsToArchive) {
            BookingHistory history = new BookingHistory(
                    null,
                    booking.getBike(),
                    booking.getUser(),
                    booking.getBookingStartDate(),
                    booking.getBookingEndDate()
            );
            bookingHistoryRepository.save(history);
            bookingRepository.delete(booking);
        }

        logger.info("Successfully archived {} bookings.", bookingsToArchive.size());
    }
}
