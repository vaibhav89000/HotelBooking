package com.hotelbooking.hotel_booking.service.cancellation;

import com.hotelbooking.hotel_booking.model.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CancellationPolicy {

    BigDecimal refundAmount(Booking booking, LocalDate cancellationDate);
}
