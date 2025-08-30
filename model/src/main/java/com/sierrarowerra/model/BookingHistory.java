package com.sierrarowerra.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;

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
    private ZonedDateTime createdAt;
}
