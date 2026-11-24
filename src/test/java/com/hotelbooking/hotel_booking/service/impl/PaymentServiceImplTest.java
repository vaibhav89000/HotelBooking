package com.hotelbooking.hotel_booking.service.impl;

import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import com.hotelbooking.hotel_booking.enums.PaymentStatus;
import com.hotelbooking.hotel_booking.exception.DomainValidationException;
import com.hotelbooking.hotel_booking.lock.InMemoryLockManager;
import com.hotelbooking.hotel_booking.model.Booking;
import com.hotelbooking.hotel_booking.model.DateRange;
import com.hotelbooking.hotel_booking.model.Payment;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryBookingRepository;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryPaymentRepository;
import com.hotelbooking.hotel_booking.service.payment.CardPaymentProcessor;
import com.hotelbooking.hotel_booking.service.payment.PaymentGateway;
import com.hotelbooking.hotel_booking.service.payment.PaymentGatewayResult;
import com.hotelbooking.hotel_booking.service.payment.PaymentProcessor;
import com.hotelbooking.hotel_booking.service.payment.UpiPaymentProcessor;
import com.hotelbooking.hotel_booking.service.payment.WalletPaymentProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentServiceImplTest {

    private final InMemoryBookingRepository bookingRepository = new InMemoryBookingRepository();
    private final InMemoryPaymentRepository paymentRepository = new InMemoryPaymentRepository();
    private final AtomicInteger gatewayCalls = new AtomicInteger();
    private boolean gatewaySucceeds = true;
    private PaymentServiceImpl paymentService;
    private Booking booking;

    @BeforeEach
    void setUp() {
        PaymentGateway gateway = (method, amount, key) -> {
            gatewayCalls.incrementAndGet();
            return gatewaySucceeds
                    ? PaymentGatewayResult.success("gateway-ref-" + key)
                    : PaymentGatewayResult.failure("declined");
        };
        List<PaymentProcessor> processors = List.of(
                new CardPaymentProcessor(gateway),
                new UpiPaymentProcessor(gateway),
                new WalletPaymentProcessor(gateway));
        paymentService = new PaymentServiceImpl(
                bookingRepository, paymentRepository, processors, new InMemoryLockManager());
        booking = new Booking("booking-1", "property-1", "room-1",
                DateRange.of(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12)),
                2, new BigDecimal("7000"));
        bookingRepository.save(booking);
    }

    @Test
    void successfulPaymentConfirmsBooking() {
        Payment payment = paymentService.pay(booking.getId(), PaymentMethodType.UPI, "key-1");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(payment.getAmount()).isEqualByComparingTo("7000");
        assertThat(gatewayCalls).hasValue(1);
    }

    @Test
    void failedPaymentLeavesBookingPendingAndCanBeRetriedWithNewKey() {
        gatewaySucceeds = false;
        Payment failed = paymentService.pay(booking.getId(), PaymentMethodType.CARD, "key-failed");

        assertThat(failed.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);

        gatewaySucceeds = true;
        Payment retried = paymentService.pay(booking.getId(), PaymentMethodType.WALLET, "key-retry");

        assertThat(retried.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(gatewayCalls).hasValue(2);
    }

    @Test
    void sameIdempotencyKeyDoesNotChargeTwice() {
        Payment first = paymentService.pay(booking.getId(), PaymentMethodType.UPI, "same-key");
        Payment replay = paymentService.pay(booking.getId(), PaymentMethodType.UPI, "same-key");

        assertThat(replay).isSameAs(first);
        assertThat(gatewayCalls).hasValue(1);
        assertThat(paymentRepository.findByBookingId(booking.getId())).hasSize(1);
    }

    @Test
    void rejectsReusingKeyWithDifferentRequest() {
        paymentService.pay(booking.getId(), PaymentMethodType.UPI, "same-key");

        assertThatThrownBy(() -> paymentService.pay(booking.getId(), PaymentMethodType.CARD, "same-key"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("different payment request");
        assertThat(gatewayCalls).hasValue(1);
    }

    @Test
    void doesNotAllowSecondPaymentAfterBookingIsConfirmed() {
        paymentService.pay(booking.getId(), PaymentMethodType.UPI, "key-1");

        assertThatThrownBy(() -> paymentService.pay(booking.getId(), PaymentMethodType.CARD, "key-2"))
                .isInstanceOf(DomainValidationException.class);
        assertThat(gatewayCalls).hasValue(1);
    }
}
