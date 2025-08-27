package com.sierrarowerra.controller;

import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.dto.BookingRequestDto;
import com.sierrarowerra.model.dto.BookingResponseDto;
import com.sierrarowerra.security.services.UserDetailsImpl;
import com.sierrarowerra.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequestDto bookingRequest,
                                                 @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Booking newBooking = bookingService.createBooking(bookingRequest, currentUser.getId());
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    @GetMapping
    public List<BookingResponseDto> getAllBookings(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        Set<String> roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return bookingService.findAll(currentUser.getId(), roles);
    }
}
