package com.sierrarowerra.domain;

import com.sierrarowerra.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.bike.id = :bikeId AND b.bookingStartDate < :endDate AND b.bookingEndDate > :startDate")
    List<Booking> findOverlappingBookings(@Param("bikeId") Long bikeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    boolean existsByBikeId(Long bikeId);
}
