package com.sierrarowerra.controller;

import com.sierrarowerra.model.Payment;
import com.sierrarowerra.model.dto.PaymentDto;
import com.sierrarowerra.security.services.UserDetailsImpl;
import com.sierrarowerra.services.PaymentService;
import com.sierrarowerra.services.mapper.PaymentMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation(summary = "Get a paginated list of payments (for admins: all; for users: their own)")
    @GetMapping
    public Page<PaymentDto> getPayments(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                        @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Set<String> roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return paymentService.findPaymentsForCurrentUser(currentUser.getId(), roles, pageable);
    }

    @Operation(summary = "Get a paginated list of payments for a specific user (Admin only)")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PaymentDto> getPaymentsByUserId(@PathVariable Long userId,
                                                @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return paymentService.findPaymentsByUserId(userId, pageable);
    }
}
