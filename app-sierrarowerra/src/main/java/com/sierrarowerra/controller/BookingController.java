package com.sierrarowerra.controller;

import com.sierrarowerra.model.Payment;
import com.sierrarowerra.model.dto.BookingCreationResponseDto;
import com.sierrarowerra.model.dto.BookingRequestDto;
import com.sierrarowerra.model.dto.BookingResponseDto;
import com.sierrarowerra.model.dto.PageDto;
import com.sierrarowerra.model.dto.payload.BookingExtensionRequestDto;
import com.sierrarowerra.security.services.UserDetailsImpl;
import com.sierrarowerra.services.BookingService;
import com.sierrarowerra.services.mapper.PageMapper;
import com.sierrarowerra.services.mapper.PaymentMapper;
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
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final PaymentMapper paymentMapper;
    private final PageMapper pageMapper;

    @Operation(summary = "Create a new booking for a bike")
    @PostMapping
    public ResponseEntity<BookingCreationResponseDto> createBooking(@Valid @RequestBody BookingRequestDto bookingRequest,
                                                                    @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Payment createdPayment = bookingService.createBooking(bookingRequest, currentUser.getId());

        BookingCreationResponseDto responseDto = new BookingCreationResponseDto();
        responseDto.setBookingId(createdPayment.getBooking().getId());
        responseDto.setPaymentDetails(paymentMapper.toDto(createdPayment));

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a paginated list of active bookings (for admins: all; for users: their own)")
    @GetMapping
    public PageDto<BookingResponseDto> getAllBookings(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                                      @ParameterObject @PageableDefault(sort = "bookingStartDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Set<String> roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        Page<BookingResponseDto> bookingPage = bookingService.findAll(currentUser.getId(), roles, pageable);
        return pageMapper.toDto(bookingPage, booking -> booking);
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
    public PageDto<BookingResponseDto> getBookingHistory(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                                         @ParameterObject @PageableDefault(sort = "bookingEndDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Set<String> roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        Page<BookingResponseDto> historyPage = bookingService.getBookingHistory(currentUser.getId(), roles, pageable);
        return pageMapper.toDto(historyPage, booking -> booking);
    }

    @Operation(summary = "Extend an existing booking")
    @PostMapping("/{bookingId}/extend")
    public ResponseEntity<BookingCreationResponseDto> extendBooking(@PathVariable Long bookingId,
                                                                    @Valid @RequestBody BookingExtensionRequestDto extensionRequest,
                                                                    @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Set<String> roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        Payment extensionPayment = bookingService.extendBooking(bookingId, extensionRequest, currentUser.getId(), roles);

        BookingCreationResponseDto responseDto = new BookingCreationResponseDto();
        responseDto.setBookingId(extensionPayment.getBooking().getId());
        responseDto.setPaymentDetails(paymentMapper.toDto(extensionPayment));

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
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
