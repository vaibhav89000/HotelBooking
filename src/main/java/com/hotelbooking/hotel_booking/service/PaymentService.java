package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import com.hotelbooking.hotel_booking.model.Payment;

public interface PaymentService {

    Payment pay(String bookingId, PaymentMethodType method, String idempotencyKey);
}
