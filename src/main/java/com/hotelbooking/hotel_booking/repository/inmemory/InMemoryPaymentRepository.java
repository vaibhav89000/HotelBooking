package com.hotelbooking.hotel_booking.repository.inmemory;

import com.hotelbooking.hotel_booking.model.Payment;
import com.hotelbooking.hotel_booking.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryPaymentRepository extends InMemoryRepository<Payment> implements PaymentRepository {

    public InMemoryPaymentRepository() {
        super(Payment::getId);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return findWhere(payment -> payment.getIdempotencyKey().equals(idempotencyKey))
                .stream()
                .findFirst();
    }

    @Override
    public List<Payment> findByBookingId(String bookingId) {
        return findWhere(payment -> payment.getBookingId().equals(bookingId));
    }
}
