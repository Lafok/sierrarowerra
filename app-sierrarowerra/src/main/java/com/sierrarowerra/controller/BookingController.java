package com.sierrarowerra.controller;

import com.sierrarowerra.model.Booking;
import com.sierrarowerra.model.dto.BookingRequestDto;
import com.sierrarowerra.model.dto.BookingResponseDto;
import com.sierrarowerra.security.services.UserDetailsImpl;
import com.sierrarowerra.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final List<String> validSortFields = List.of("id", "bookingStartDate", "bookingEndDate");

    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequestDto bookingRequest,
                                                 @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Booking newBooking = bookingService.createBooking(bookingRequest, currentUser.getId());
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<BookingResponseDto> getAllBookings(@AuthenticationPrincipal UserDetailsImpl currentUser, Pageable pageable) {
        // Sanitize sort parameters to prevent PropertyReferenceException
        Sort sanitizedSort = Sort.by(pageable.getSort().stream()
                .filter(order -> validSortFields.contains(order.getProperty()))
                .collect(Collectors.toList()));

        // If no valid sort fields are provided (or if the only one was 'string'), use a default
        if (sanitizedSort.isEmpty()) {
            sanitizedSort = Sort.by(Sort.Direction.DESC, "bookingStartDate");
        }

        Pageable effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sanitizedSort);

        Set<String> roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return bookingService.findAll(currentUser.getId(), roles, effectivePageable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Set<String> roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        bookingService.deleteBooking(id, currentUser.getId(), roles);
        return ResponseEntity.noContent().build();
    }
}
