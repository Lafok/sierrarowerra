package com.sierrarowerra.domain;

import com.sierrarowerra.model.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    Optional<PaymentHistory> findByBookingId(Long bookingId);
}
