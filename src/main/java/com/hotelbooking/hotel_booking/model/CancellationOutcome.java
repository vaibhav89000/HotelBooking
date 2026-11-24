package com.hotelbooking.hotel_booking.model;

import java.math.BigDecimal;

public record CancellationOutcome(Booking booking, Payment payment, BigDecimal refundAmount) {
}
