package com.sierrarowerra.tasks;

import com.sierrarowerra.services.BookingArchivingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingArchiverTask {

    private final BookingArchivingService bookingArchivingService;

    /**
     * Runs every day at 1:00 AM.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void archiveOldBookings() {
        bookingArchivingService.archiveOldBookings();
    }
}
