package com.sierrarowerra.controller;

import com.sierrarowerra.model.Payment;
import com.sierrarowerra.model.dto.PaymentDto;
import com.sierrarowerra.security.services.UserDetailsImpl;
import com.sierrarowerra.services.PaymentService;
import com.sierrarowerra.services.mapper.PaymentMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @Operation(summary = "Simulate processing a payment for a booking")
    @PostMapping("/bookings/{bookingId}/pay")
    public ResponseEntity<PaymentDto> payForBooking(@PathVariable Long bookingId, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Set<String> roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        Payment processedPayment = paymentService.processPaymentForBooking(bookingId, currentUser.getId(), roles);
        PaymentDto paymentDto = paymentMapper.toDto(processedPayment);

        return ResponseEntity.ok(paymentDto);
    }
}
