package com.sierrarowerra.controller;

import com.sierrarowerra.services.stripe.StripeWebhookService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/stripe")
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripeWebhookService stripeWebhookService;
    private final String webhookSecret;

    public StripeWebhookController(StripeWebhookService stripeWebhookService, @Value("${stripe.webhook.secret}") String webhookSecret) {
        this.stripeWebhookService = stripeWebhookService;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        if (payload == null || sigHeader == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing payload or signature header.");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            logger.warn("Webhook signature verification failed.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature verification failed.");
        } catch (Exception e) {
            logger.error("Error parsing webhook event.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error: Could not parse event.");
        }

        logger.info("Received and verified Stripe event: {} ({})", event.getId(), event.getType());

        Optional<StripeObject> stripeObjectOptional = event.getDataObjectDeserializer().getObject();

        if (stripeObjectOptional.isEmpty()) {
            logger.warn("Standard deserialization failed for event {}. Attempting unsafe deserialization.", event.getId());
            try {
                stripeObjectOptional = Optional.ofNullable(event.getDataObjectDeserializer().deserializeUnsafe());
            } catch (EventDataObjectDeserializationException e) {
                logger.error("FATAL: Unsafe deserialization also failed for event {}: {}", event.getId(), e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error: Cannot deserialize event data.");
            }
        }

        if (stripeObjectOptional.isEmpty()) {
            logger.error("FATAL: Could not deserialize event data object for event {} after all attempts.", event.getId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error: Event data is empty or un-parseable.");
        }

        StripeObject stripeObject = stripeObjectOptional.get();

        switch (event.getType()) {
            case "payment_intent.created":
                if (stripeObject instanceof PaymentIntent paymentIntent) {
                    logger.info("PaymentIntent {} was created for bookingId: {}", paymentIntent.getId(), paymentIntent.getMetadata().get("bookingId"));
                }
                break;
            case "payment_intent.succeeded":
                if (stripeObject instanceof PaymentIntent paymentIntent) {
                    stripeWebhookService.handlePaymentSucceeded(paymentIntent);
                } else {
                    logger.error("Event data for 'payment_intent.succeeded' is not a PaymentIntent. Type: {}", stripeObject.getClass().getName());
                }
                break;
            case "payment_intent.payment_failed":
                if (stripeObject instanceof PaymentIntent failedPaymentIntent) {
                    stripeWebhookService.handlePaymentFailed(failedPaymentIntent);
                } else {
                    logger.error("Event data for 'payment_intent.payment_failed' is not a PaymentIntent. Type: {}", stripeObject.getClass().getName());
                }
                break;
            default:
                break;
        }

        return ResponseEntity.ok().build();
    }
}
