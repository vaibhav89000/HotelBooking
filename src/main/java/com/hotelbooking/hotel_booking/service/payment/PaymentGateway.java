package com.hotelbooking.hotel_booking.service.payment;

import com.hotelbooking.hotel_booking.enums.PaymentMethodType;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentGatewayResult charge(PaymentMethodType method, BigDecimal amount, String idempotencyKey);

    default PaymentGatewayResult refund(String gatewayReference, BigDecimal amount, String idempotencyKey) {
        return PaymentGatewayResult.failure("Refund is not supported by this gateway");
    }
}
