package com.hotelbooking.hotel_booking.service.payment;

import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import com.hotelbooking.hotel_booking.model.Payment;

public interface PaymentProcessor {

    PaymentMethodType method();

    PaymentGatewayResult process(Payment payment);
}
