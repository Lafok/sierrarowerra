package com.sierrarowerra.controller;

import com.sierrarowerra.model.dto.booking.BookingRequestDto;
import com.sierrarowerra.model.dto.booking.BookingResponseDto;
import com.sierrarowerra.model.dto.payment.PaymentInitiationResponseDto;
import com.sierrarowerra.model.dto.booking.BookingExtensionRequestDto;
import com.sierrarowerra.security.services.UserDetailsImpl;
import com.sierrarowerra.services.booking.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a new booking and initiate the payment process")
    @PostMapping
    public ResponseEntity<PaymentInitiationResponseDto> createBooking(@Valid @RequestBody BookingRequestDto bookingRequest,
                                                                      @AuthenticationPrincipal UserDetailsImpl currentUser) {
        PaymentInitiationResponseDto response = bookingService.initiateBooking(bookingRequest, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Initiate a payment to extend an existing booking")
    @PostMapping("/{bookingId}/extend")
    public ResponseEntity<PaymentInitiationResponseDto> extendBooking(@PathVariable Long bookingId,
                                                                      @Valid @RequestBody BookingExtensionRequestDto extensionRequest,
                                                                      @AuthenticationPrincipal UserDetailsImpl currentUser) {
        PaymentInitiationResponseDto responseDto = bookingService.extendBooking(bookingId, extensionRequest, currentUser.getId(), extractRoles(currentUser));
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Get a paginated list of active bookings (for admins: all; for users: their own)")
    @GetMapping
    public Page<BookingResponseDto> getAllBookings(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                                   @ParameterObject @PageableDefault(sort = "bookingStartDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return bookingService.findAll(currentUser.getId(), extractRoles(currentUser), pageable);
    }

    @Operation(summary = "Get a specific active booking by its ID (admins can see any, users can only see their own)")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return bookingService.findBookingById(id, currentUser.getId(), extractRoles(currentUser))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get a paginated list of past bookings (booking history)")
    @GetMapping("/history")
    public Page<BookingResponseDto> getBookingHistory(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                                      @ParameterObject @PageableDefault(sort = "bookingEndDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return bookingService.getBookingHistory(currentUser.getId(), extractRoles(currentUser), pageable);
    }

    @Operation(summary = "Delete a booking (admins can delete any, users can only delete their own)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        bookingService.deleteBooking(id, currentUser.getId(), extractRoles(currentUser));
        return ResponseEntity.noContent().build();
    }

    private Set<String> extractRoles(UserDetailsImpl currentUser) {
        return currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    @Operation(summary = "Get a list of all booked dates for a specific bike")
    @GetMapping("/bike/{bikeId}/dates")
    public ResponseEntity<List<String>> getBookedDatesForBike(@PathVariable Long bikeId) {
        List<String> bookedDates = bookingService.getBookedDatesForBike(bikeId);
        return ResponseEntity.ok(bookedDates);
    }
}
