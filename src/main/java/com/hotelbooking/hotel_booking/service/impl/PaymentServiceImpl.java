package com.hotelbooking.hotel_booking.service.impl;

import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import com.hotelbooking.hotel_booking.exception.DomainValidationException;
import com.hotelbooking.hotel_booking.exception.ResourceNotFoundException;
import com.hotelbooking.hotel_booking.lock.LockManager;
import com.hotelbooking.hotel_booking.model.Booking;
import com.hotelbooking.hotel_booking.model.Payment;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import com.hotelbooking.hotel_booking.repository.PaymentRepository;
import com.hotelbooking.hotel_booking.service.PaymentService;
import com.hotelbooking.hotel_booking.service.payment.PaymentGatewayResult;
import com.hotelbooking.hotel_booking.service.payment.PaymentProcessor;
import com.hotelbooking.hotel_booking.util.Guard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final List<PaymentProcessor> processors;
    private final LockManager lockManager;

    @Override
    public Payment pay(String bookingId, PaymentMethodType method, String idempotencyKey) {
        Guard.notBlank(bookingId, "bookingId");
        Guard.notNull(method, "method");
        String key = Guard.notBlank(idempotencyKey, "Idempotency-Key");

        // Serialize a key globally, then serialize payment attempts for the target booking.
        return lockManager.withLock("payment-idempotency:" + key, () -> {
            var existing = paymentRepository.findByIdempotencyKey(key);
            if (existing.isPresent()) {
                Payment payment = existing.get();
                if (!payment.getBookingId().equals(bookingId) || payment.getMethod() != method) {
                    throw new DomainValidationException(
                            "Idempotency-Key has already been used for a different payment request");
                }
                return payment;
            }

            return lockManager.withLock("booking-payment:" + bookingId, () -> processNewPayment(bookingId, method, key));
        });
    }

    private Payment processNewPayment(String bookingId, PaymentMethodType method, String key) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() != com.hotelbooking.hotel_booking.enums.BookingStatus.PENDING_PAYMENT) {
            throw new DomainValidationException("Only bookings pending payment can be paid");
        }

        PaymentProcessor processor = processors.stream()
                .filter(candidate -> candidate.method() == method)
                .findFirst()
                .orElseThrow(() -> new DomainValidationException("Unsupported payment method: " + method));

        Payment payment = new Payment(
                UUID.randomUUID().toString(), bookingId, booking.getTotalAmount(), method, key);

        // Persist the idempotency record before calling the gateway. Replays return this outcome.
        paymentRepository.save(payment);
        try {
            PaymentGatewayResult result = processor.process(payment);
            if (result != null && result.successful()) {
                payment.markSucceeded(result.reference());
                booking.confirm();
                bookingRepository.save(booking);
            } else {
                payment.markFailed();
            }
        } catch (RuntimeException gatewayFailure) {
            // A failed attempt remains retryable with a new key. The same key replays this result.
            payment.markFailed();
        }

        return paymentRepository.save(payment);
    }
}
