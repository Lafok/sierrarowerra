package com.sierrarowerra.services;

import com.sierrarowerra.domain.booking.BookingRepository;
import com.sierrarowerra.domain.payment.PaymentRepository;
import com.sierrarowerra.domain.booking.Booking;
import com.sierrarowerra.model.enums.BookingStatus;
import com.sierrarowerra.domain.payment.Payment;
import com.sierrarowerra.model.enums.PaymentStatus;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StripeWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookService.class);
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    public StripeWebhookService(BookingRepository bookingRepository, PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void handlePaymentSucceeded(PaymentIntent paymentIntent) {
        String bookingIdStr = paymentIntent.getMetadata().get("bookingId");
        if (bookingIdStr == null) {
            logger.error("Missing bookingId in PaymentIntent metadata for pi_id: {}", paymentIntent.getId());
            return;
        }

        long bookingId = Long.parseLong(bookingIdStr);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found for id: " + bookingId));

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalStateException("Payment not found for booking id: " + bookingId));

        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setExpiresAt(null);
            bookingRepository.save(booking);

            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            logger.info("Booking {} confirmed and payment {} marked as COMPLETED.", booking.getId(), payment.getId());
        } else {
            logger.warn("Received successful payment webhook for booking {} which is not in PENDING_PAYMENT state (current: {}). Ignoring.", booking.getId(), booking.getStatus());
        }
    }

    @Transactional
    public void handlePaymentFailed(PaymentIntent paymentIntent) {
        String bookingIdStr = paymentIntent.getMetadata().get("bookingId");
        if (bookingIdStr == null) {
            logger.error("Missing bookingId in PaymentIntent metadata for failed pi_id: {}", paymentIntent.getId());
            return;
        }

        long bookingId = Long.parseLong(bookingIdStr);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found for id: " + bookingId));

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalStateException("Payment not found for booking id: " + bookingId));

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        logger.warn("Payment failed for booking {}. Reason: {}. Booking and Payment status updated.", 
                bookingIdStr, paymentIntent.getLastPaymentError() != null ? paymentIntent.getLastPaymentError().getMessage() : "N/A");
    }
}
