package com.hotelbooking.hotel_booking.dto.response;

import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.model.Booking;
import com.hotelbooking.hotel_booking.model.Payment;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class CancellationResponse {

    String bookingId;
    BookingStatus status;
    BigDecimal refundAmount;
    String paymentId;

    public static CancellationResponse from(Booking booking, Payment payment, BigDecimal refundAmount) {
        return new CancellationResponse(
                booking.getId(),
                booking.getStatus(),
                refundAmount,
                payment == null ? null : payment.getId());
    }
}
