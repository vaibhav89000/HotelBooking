package com.hotelbooking.hotel_booking.service.payment;

import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import com.hotelbooking.hotel_booking.model.Payment;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractGatewayPaymentProcessor implements PaymentProcessor {

    private final PaymentGateway paymentGateway;

    @Override
    public PaymentGatewayResult process(Payment payment) {
        return paymentGateway.charge(method(), payment.getAmount(), payment.getIdempotencyKey());
    }
}
