package com.sierrarowerra.services;

import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.dto.BookingRequestDto;

public interface BookingService {
    Booking createBooking(BookingRequestDto bookingRequest);
}
