package com.sierrarowerra.services.booking.impl;

import com.sierrarowerra.domain.bike.Bike;
import com.sierrarowerra.domain.bike.BikeRepository;
import com.sierrarowerra.domain.booking.Booking;
import com.sierrarowerra.domain.booking.BookingHistoryRepository;
import com.sierrarowerra.domain.booking.BookingRepository;
import com.sierrarowerra.domain.payment.Payment;
import com.sierrarowerra.domain.payment.PaymentHistoryRepository;
import com.sierrarowerra.domain.payment.PaymentRepository;
import com.sierrarowerra.domain.tariff.Tariff;
import com.sierrarowerra.domain.user.User;
import com.sierrarowerra.domain.user.UserRepository;
import com.sierrarowerra.model.dto.booking.BookingRequestDto;
import com.sierrarowerra.model.enums.BikeStatus;
import com.sierrarowerra.model.enums.BookingStatus;
import com.sierrarowerra.model.enums.PaymentStatus;
import com.sierrarowerra.model.enums.TariffType;
import com.sierrarowerra.services.booking.mapper.BookingMapper;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingHistoryRepository bookingHistoryRepository;
    @Mock private BikeRepository bikeRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentHistoryRepository paymentHistoryRepository;
    @Mock private PlatformTransactionManager transactionManager;
    @Mock private BookingMapper bookingMapper;
    @Mock private TransactionStatus transactionStatus;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        bookingService = new BookingServiceImpl(
                bookingRepository,
                bookingHistoryRepository,
                bikeRepository,
                userRepository,
                paymentRepository,
                paymentHistoryRepository,
                bookingMapper,
                transactionManager,
                "test_key"
        );
    }
    @Test
    void initiateBooking_ShouldThrowException_WhenBikeNotAvailable() {
        BookingRequestDto request = new BookingRequestDto();
        request.setBikeId(1L);
        
        Bike bike = new Bike();
        bike.setStatus(BikeStatus.MAINTENANCE);
        
        when(bikeRepository.findAndLockById(1L)).thenReturn(Optional.of(bike));

        assertThrows(IllegalStateException.class, () -> bookingService.initiateBooking(request, 1L));
    }

    @Test
    void initiateBooking_ShouldThrowException_WhenOverlappingExists() {
        BookingRequestDto request = new BookingRequestDto();
        request.setBikeId(1L);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));

        Bike bike = new Bike();
        bike.setStatus(BikeStatus.AVAILABLE);
        
        when(bikeRepository.findAndLockById(1L)).thenReturn(Optional.of(bike));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(bookingRepository.findOverlappingBookings(any(), any(), any()))
                .thenReturn(Collections.singletonList(new Booking()));

        assertThrows(IllegalStateException.class, () -> bookingService.initiateBooking(request, 1L));
    }

    @Test
    void deleteBooking_ShouldCancelStripeIntent_WhenPaymentPending() throws Exception {
        Booking booking = new Booking();
        booking.setId(100L);
        User user = new User();
        user.setId(1L);
        booking.setUser(user);

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentIntentId("pi_123");

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(100L)).thenReturn(Optional.of(payment));

        // Mocking Stripe static call
        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            mockedPaymentIntent.when(() -> PaymentIntent.retrieve("pi_123")).thenReturn(mockIntent);

            bookingService.deleteBooking(100L, 1L, Collections.emptySet());

            verify(mockIntent).cancel();
            verify(bookingRepository).delete(booking);
        }
    }
}
