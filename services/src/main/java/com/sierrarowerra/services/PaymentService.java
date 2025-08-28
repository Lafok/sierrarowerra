package com.sierrarowerra.services;

import com.sierrarowerra.model.Payment;

import java.util.Set;

public interface PaymentService {
    Payment processPaymentForBooking(Long bookingId, Long userId, Set<String> roles);
}
