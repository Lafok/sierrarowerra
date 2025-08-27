package com.sierrarowerra.domain;

import com.sierrarowerra.model.BookingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {
    Page<BookingHistory> findByUserId(Long userId, Pageable pageable);
}
