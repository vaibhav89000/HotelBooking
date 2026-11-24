package com.hotelbooking.hotel_booking.service.impl;

import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.enums.PaymentStatus;
import com.hotelbooking.hotel_booking.exception.DomainValidationException;
import com.hotelbooking.hotel_booking.exception.ResourceNotFoundException;
import com.hotelbooking.hotel_booking.lock.LockManager;
import com.hotelbooking.hotel_booking.model.Booking;
import com.hotelbooking.hotel_booking.model.CancellationOutcome;
import com.hotelbooking.hotel_booking.model.Payment;
import com.hotelbooking.hotel_booking.model.RoomInventory;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import com.hotelbooking.hotel_booking.repository.PaymentRepository;
import com.hotelbooking.hotel_booking.repository.RoomInventoryRepository;
import com.hotelbooking.hotel_booking.service.CancellationService;
import com.hotelbooking.hotel_booking.service.cancellation.CancellationPolicy;
import com.hotelbooking.hotel_booking.service.payment.PaymentGateway;
import com.hotelbooking.hotel_booking.service.payment.PaymentGatewayResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CancellationServiceImpl implements CancellationService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RoomInventoryRepository inventoryRepository;
    private final CancellationPolicy cancellationPolicy;
    private final PaymentGateway paymentGateway;
    private final LockManager lockManager;

    @Override
    public CancellationOutcome cancel(String bookingId) {
        return lockManager.withLock("booking-payment:" + bookingId, () -> {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

            if (booking.getStatus() == BookingStatus.CANCELLED) {
                Payment previousPayment = successfulOrRefundedPayment(bookingId);
                BigDecimal alreadyRefunded = previousPayment == null
                        ? BigDecimal.ZERO : previousPayment.getRefundedAmount();
                return new CancellationOutcome(booking, previousPayment, alreadyRefunded);
            }

            Payment payment = successfulOrRefundedPayment(bookingId);
            if (payment != null && payment.getStatus() == PaymentStatus.REFUNDED) {
                throw new DomainValidationException("Booking payment was already refunded");
            }

            BigDecimal refundAmount = payment == null
                    ? BigDecimal.ZERO
                    : cancellationPolicy.refundAmount(booking, LocalDate.now());

            return lockManager.withLock(booking.getRoomTypeId(), () -> {
                if (payment != null) {
                    PaymentGatewayResult refundResult = paymentGateway.refund(
                            payment.getGatewayReference(), refundAmount,
                            "refund:" + booking.getId());
                    if (refundResult == null || !refundResult.successful()) {
                        throw new DomainValidationException("Payment refund failed; booking was not cancelled");
                    }
                    payment.refund(refundAmount);
                    paymentRepository.save(payment);
                }

                RoomInventory inventory = inventoryRepository.findById(booking.getRoomTypeId())
                        .orElseThrow(() -> new ResourceNotFoundException("RoomInventory", booking.getRoomTypeId()));
                inventory.release(booking.getStay());
                booking.cancel();
                bookingRepository.save(booking);
                return new CancellationOutcome(booking, payment, refundAmount);
            });
        });
    }

    private Payment successfulOrRefundedPayment(String bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.SUCCEEDED
                        || payment.getStatus() == PaymentStatus.REFUNDED)
                .findFirst()
                .orElse(null);
    }
}
