package com.hotelbooking.hotel_booking.service.payment;

public record PaymentGatewayResult(boolean successful, String reference, String failureReason) {

    public static PaymentGatewayResult success(String reference) {
        return new PaymentGatewayResult(true, reference, null);
    }

    public static PaymentGatewayResult failure(String reason) {
        return new PaymentGatewayResult(false, null, reason);
    }
}
