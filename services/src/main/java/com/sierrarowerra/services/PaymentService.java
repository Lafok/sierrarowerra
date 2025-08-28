package com.sierrarowerra.services;

import com.sierrarowerra.model.Payment;
import com.sierrarowerra.model.dto.PaymentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface PaymentService {
    Payment processPaymentForBooking(Long bookingId, Long userId, Set<String> roles);

    Page<PaymentDto> findPaymentsForCurrentUser(Long userId, Set<String> roles, Pageable pageable);

    Page<PaymentDto> findPaymentsByUserId(Long userId, Pageable pageable);
}
