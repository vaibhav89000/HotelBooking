package com.hotelbooking.hotel_booking.repository;

import com.hotelbooking.hotel_booking.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends BaseRepository<Payment> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByBookingId(String bookingId);
}
