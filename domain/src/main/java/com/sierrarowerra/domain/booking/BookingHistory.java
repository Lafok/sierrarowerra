package com.sierrarowerra.domain.booking;

import com.sierrarowerra.domain.bike.Bike;
import com.sierrarowerra.domain.user.User;
import com.sierrarowerra.model.enums.ArchivalReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking_history")
public class BookingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId; // The ID of the original booking

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bike_id", nullable = false)
    private Bike bike;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate bookingStartDate;

    @Column(nullable = false)
    private LocalDate bookingEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArchivalReason reason;

    @Column(nullable = false)
    private Instant createdAt;
}
