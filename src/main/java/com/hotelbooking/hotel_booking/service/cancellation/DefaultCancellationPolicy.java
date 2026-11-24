package com.hotelbooking.hotel_booking.service.cancellation;

import com.hotelbooking.hotel_booking.model.Booking;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DefaultCancellationPolicy implements CancellationPolicy {

    private static final BigDecimal HALF = new BigDecimal("0.50");

    @Override
    public BigDecimal refundAmount(Booking booking, LocalDate cancellationDate) {
        if (!cancellationDate.isAfter(booking.getStay().getCheckIn().minusDays(2))) {
            return booking.getTotalAmount();
        }
        return booking.getTotalAmount().multiply(HALF);
    }
}
