package com.hotelbooking.hotel_booking.service.payment;

import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class MockPaymentGateway implements PaymentGateway {

    private final ConcurrentMap<String, PaymentGatewayResult> results = new ConcurrentHashMap<>();

    @Override
    public PaymentGatewayResult charge(PaymentMethodType method, BigDecimal amount, String idempotencyKey) {
        return results.computeIfAbsent("charge:" + idempotencyKey,
                key -> PaymentGatewayResult.success("mock-" + UUID.randomUUID()));
    }

    @Override
    public PaymentGatewayResult refund(String gatewayReference, BigDecimal amount, String idempotencyKey) {
        return results.computeIfAbsent("refund:" + idempotencyKey,
                key -> PaymentGatewayResult.success("mock-refund-" + UUID.randomUUID()));
    }
}
