package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import com.hotelbooking.hotel_booking.enums.PaymentStatus;
import com.hotelbooking.hotel_booking.exception.DomainValidationException;
import com.hotelbooking.hotel_booking.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private static final BigDecimal AMOUNT = new BigDecimal("6000");

    @Test
    void successfulPaymentRecordsGatewayReference() {
        Payment payment = newPayment();

        payment.markSucceeded("gw-123");

        assertThat(payment.isSuccessful()).isTrue();
        assertThat(payment.getGatewayReference()).isEqualTo("gw-123");
    }

    @Test
    void failedPaymentIsTerminal() {
        Payment payment = newPayment();
        payment.markFailed();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThatThrownBy(() -> payment.markSucceeded("gw-123"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void onlySuccessfulPaymentsCanBeRefunded() {
        Payment payment = newPayment();

        assertThatThrownBy(() -> payment.refund(AMOUNT))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void supportsPartialRefund() {
        Payment payment = newPayment();
        payment.markSucceeded("gw-123");

        payment.refund(new BigDecimal("3000"));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getRefundedAmount()).isEqualByComparingTo("3000");
    }

    @Test
    void refundCannotExceedAmountPaid() {
        Payment payment = newPayment();
        payment.markSucceeded("gw-123");

        assertThatThrownBy(() -> payment.refund(new BigDecimal("6000.01")))
                .isInstanceOf(DomainValidationException.class);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
    }

    private static Payment newPayment() {
        return new Payment("pay-1", "b-1", AMOUNT, PaymentMethodType.UPI, "idem-1");
    }
}
