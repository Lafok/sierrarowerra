package com.sierrarowerra.model;

public enum ArchivalReason {
    COMPLETED,          // Booking was completed successfully
    PAYMENT_EXPIRED,    // User failed to pay within the time limit
    CANCELLED_BY_USER   // User actively cancelled the booking
}
