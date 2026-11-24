package com.hotelbooking.hotel_booking.dto.response;

import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import com.hotelbooking.hotel_booking.enums.PaymentStatus;
import com.hotelbooking.hotel_booking.model.Payment;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class PaymentResponse {

    String id;
    String bookingId;
    BigDecimal amount;
    PaymentMethodType method;
    PaymentStatus status;
    String gatewayReference;

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getGatewayReference());
    }
}
