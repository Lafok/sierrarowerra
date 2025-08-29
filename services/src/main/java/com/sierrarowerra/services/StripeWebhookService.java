package com.sierrarowerra.services;

import com.sierrarowerra.domain.BookingRepository;
import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.BookingStatus;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StripeWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookService.class);
    private final BookingRepository bookingRepository;

    public StripeWebhookService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public void handlePaymentSucceeded(PaymentIntent paymentIntent) {
        String bookingIdStr = paymentIntent.getMetadata().get("bookingId");
        if (bookingIdStr == null) {
            logger.error("Missing bookingId in PaymentIntent metadata for pi_id: {}", paymentIntent.getId());
            return;
        }

        long bookingId;
        try {
            bookingId = Long.parseLong(bookingIdStr);
        } catch (NumberFormatException e) {
            logger.error("Invalid bookingId format in metadata for pi_id: {}. Value: '{}'", paymentIntent.getId(), bookingIdStr);
            return;
        }

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);

        if (bookingOptional.isEmpty()) {
            logger.error("Booking with ID {} not found for successful PaymentIntent {}", bookingId, paymentIntent.getId());
            return;
        }

        Booking booking = bookingOptional.get();
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setExpiresAt(null);
            bookingRepository.save(booking);
            logger.info("Booking {} confirmed successfully.", booking.getId());
        } else {
            logger.warn("Received successful payment webhook for booking {} which is not in PENDING_PAYMENT state (current: {}). Ignoring.", booking.getId(), booking.getStatus());
        }
    }

    @Transactional
    public void handlePaymentFailed(PaymentIntent paymentIntent) {
        String bookingIdStr = paymentIntent.getMetadata().get("bookingId");
        if (bookingIdStr != null) {
            logger.warn("Payment failed for booking {}. Reason: {}", bookingIdStr, paymentIntent.getLastPaymentError().getMessage());
        }
    }
}
