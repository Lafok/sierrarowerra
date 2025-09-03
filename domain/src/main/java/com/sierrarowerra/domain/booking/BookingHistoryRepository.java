package com.sierrarowerra.domain.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {
    Page<BookingHistory> findByUserId(Long userId, Pageable pageable);
}
