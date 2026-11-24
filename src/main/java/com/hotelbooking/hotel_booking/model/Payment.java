package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import com.hotelbooking.hotel_booking.enums.PaymentStatus;
import com.hotelbooking.hotel_booking.util.Guard;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Payment {

    private final String id;
    private final String bookingId;
    private final BigDecimal amount;
    private final PaymentMethodType method;
    private final String idempotencyKey;
    private PaymentStatus status;
    private String gatewayReference;
    private BigDecimal refundedAmount;

    public Payment(String id, String bookingId, BigDecimal amount, PaymentMethodType method, String idempotencyKey) {
        this.id = Guard.notBlank(id, "id");
        this.bookingId = Guard.notBlank(bookingId, "bookingId");
        this.amount = Guard.nonNegative(amount, "amount");
        this.method = Guard.notNull(method, "method");
        this.idempotencyKey = Guard.notBlank(idempotencyKey, "idempotencyKey");
        this.status = PaymentStatus.INITIATED;
        this.refundedAmount = BigDecimal.ZERO;
    }

    public void markSucceeded(String gatewayReference) {
        String reference = Guard.notBlank(gatewayReference, "gatewayReference");
        status = status.transitionTo(PaymentStatus.SUCCEEDED);
        this.gatewayReference = reference;
    }

    public void markFailed() {
        status = status.transitionTo(PaymentStatus.FAILED);
    }

    public void refund(BigDecimal refund) {
        Guard.notNull(refund, "refund");
        Guard.check(refund.signum() > 0, "refund must be greater than zero");
        Guard.check(refund.compareTo(amount) <= 0, "refund cannot exceed the amount paid");
        status = status.transitionTo(PaymentStatus.REFUNDED);
        this.refundedAmount = refund;
    }

    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCEEDED;
    }
}
