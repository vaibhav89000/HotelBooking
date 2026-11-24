package com.hotelbooking.hotel_booking.service.impl;

import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import com.hotelbooking.hotel_booking.enums.PaymentStatus;
import com.hotelbooking.hotel_booking.exception.DomainValidationException;
import com.hotelbooking.hotel_booking.lock.InMemoryLockManager;
import com.hotelbooking.hotel_booking.model.Booking;
import com.hotelbooking.hotel_booking.model.DateRange;
import com.hotelbooking.hotel_booking.model.Payment;
import com.hotelbooking.hotel_booking.model.RoomInventory;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryBookingRepository;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryPaymentRepository;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryRoomInventoryRepository;
import com.hotelbooking.hotel_booking.service.cancellation.DefaultCancellationPolicy;
import com.hotelbooking.hotel_booking.service.payment.PaymentGateway;
import com.hotelbooking.hotel_booking.service.payment.PaymentGatewayResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancellationServiceImplTest {

    private final InMemoryBookingRepository bookingRepository = new InMemoryBookingRepository();
    private final InMemoryPaymentRepository paymentRepository = new InMemoryPaymentRepository();
    private final InMemoryRoomInventoryRepository inventoryRepository = new InMemoryRoomInventoryRepository();
    private final AtomicInteger refundCalls = new AtomicInteger();
    private boolean refundSucceeds = true;
    private CancellationServiceImpl cancellationService;
    private Booking booking;
    private RoomInventory inventory;

    @BeforeEach
    void setUp() {
        PaymentGateway gateway = new PaymentGateway() {
            @Override
            public PaymentGatewayResult charge(PaymentMethodType method, BigDecimal amount, String key) {
                return PaymentGatewayResult.success("charge-ref");
            }

            @Override
            public PaymentGatewayResult refund(String reference, BigDecimal amount, String key) {
                refundCalls.incrementAndGet();
                return refundSucceeds
                        ? PaymentGatewayResult.success("refund-ref")
                        : PaymentGatewayResult.failure("refund declined");
            }
        };
        cancellationService = new CancellationServiceImpl(
                bookingRepository, paymentRepository, inventoryRepository,
                new DefaultCancellationPolicy(), gateway, new InMemoryLockManager());
    }

    @Test
    void cancelsUnpaidBookingAndReleasesInventory() {
        createBooking(LocalDate.now().plusDays(10));
        inventory.reserve(booking.getStay());

        var outcome = cancellationService.cancel(booking.getId());

        assertThat(outcome.booking().getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(outcome.refundAmount()).isEqualByComparingTo("0");
        assertThat(inventory.availableOn(booking.getStay().getCheckIn())).isEqualTo(1);
        assertThat(refundCalls).hasValue(0);
    }

    @Test
    void fullRefundWhenCancelledAtLeastTwoDaysBeforeCheckIn() {
        createBooking(LocalDate.now().plusDays(2));
        inventory.reserve(booking.getStay());
        Payment payment = successfulPayment();

        var outcome = cancellationService.cancel(booking.getId());

        assertThat(outcome.refundAmount()).isEqualByComparingTo("7000");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getRefundedAmount()).isEqualByComparingTo("7000");
        assertThat(refundCalls).hasValue(1);
    }

    @Test
    void halfRefundWhenCancelledLessThanTwoDaysBeforeCheckIn() {
        createBooking(LocalDate.now().plusDays(1));
        inventory.reserve(booking.getStay());
        Payment payment = successfulPayment();

        var outcome = cancellationService.cancel(booking.getId());

        assertThat(outcome.refundAmount()).isEqualByComparingTo("3500");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(refundCalls).hasValue(1);
    }

    @Test
    void repeatedCancellationDoesNotRefundTwiceOrReleaseTwice() {
        createBooking(LocalDate.now().plusDays(5));
        inventory.reserve(booking.getStay());
        successfulPayment();

        cancellationService.cancel(booking.getId());
        var replay = cancellationService.cancel(booking.getId());

        assertThat(replay.booking().getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(replay.refundAmount()).isEqualByComparingTo("7000");
        assertThat(refundCalls).hasValue(1);
        assertThat(inventory.availableOn(booking.getStay().getCheckIn())).isEqualTo(1);
    }

    @Test
    void failedRefundLeavesBookingAndInventoryUnchanged() {
        createBooking(LocalDate.now().plusDays(5));
        inventory.reserve(booking.getStay());
        successfulPayment();
        refundSucceeds = false;

        assertThatThrownBy(() -> cancellationService.cancel(booking.getId()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("refund failed");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(inventory.availableOn(booking.getStay().getCheckIn())).isZero();
        assertThat(paymentRepository.findByBookingId(booking.getId()).getFirst().getStatus())
                .isEqualTo(PaymentStatus.SUCCEEDED);
    }

    private void createBooking(LocalDate checkIn) {
        booking = new Booking("booking-1", "property-1", "room-1",
                DateRange.of(checkIn, checkIn.plusDays(2)), 2, new BigDecimal("7000"));
        bookingRepository.save(booking);
        inventory = new RoomInventory("room-1", 1);
        inventoryRepository.save(inventory);
    }

    private Payment successfulPayment() {
        Payment payment = new Payment("payment-1", booking.getId(), booking.getTotalAmount(),
                PaymentMethodType.UPI, "payment-key");
        payment.markSucceeded("charge-ref");
        paymentRepository.save(payment);
        booking.confirm();
        bookingRepository.save(booking);
        return payment;
    }
}
