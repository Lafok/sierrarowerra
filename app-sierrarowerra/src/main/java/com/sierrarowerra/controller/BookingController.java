package com.sierrarowerra.controller;

import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.dto.BookingRequestDto;
import com.sierrarowerra.model.dto.BookingResponseDto;
import com.sierrarowerra.security.services.UserDetailsImpl;
import com.sierrarowerra.services.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a new booking for a bike")
    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequestDto bookingRequest,
                                                 @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Booking newBooking = bookingService.createBooking(bookingRequest, currentUser.getId());
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a paginated list of active bookings (for admins: all; for users: their own)")
    @GetMapping
    public Page<BookingResponseDto> getAllBookings(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                                   @ParameterObject @PageableDefault(sort = "bookingStartDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Set<String> roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return bookingService.findAll(currentUser.getId(), roles, pageable);
    }

    @Operation(summary = "Get a specific active booking by its ID (admins can see any, users can only see their own)")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Set<String> roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return bookingService.findBookingById(id, currentUser.getId(), roles)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get a paginated list of past bookings (booking history)")
    @GetMapping("/history")
    public Page<BookingResponseDto> getBookingHistory(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                                      @ParameterObject @PageableDefault(sort = "bookingEndDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Set<String> roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return bookingService.getBookingHistory(currentUser.getId(), roles, pageable);
    }

    @Operation(summary = "Delete a booking (admins can delete any, users can only delete their own)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Set<String> roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        bookingService.deleteBooking(id, currentUser.getId(), roles);
        return ResponseEntity.noContent().build();
    }
}
