package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.model.Booking;
import com.hotelbooking.hotel_booking.model.Payment;
import com.hotelbooking.hotel_booking.model.CancellationOutcome;

public interface CancellationService {

    CancellationOutcome cancel(String bookingId);
}
