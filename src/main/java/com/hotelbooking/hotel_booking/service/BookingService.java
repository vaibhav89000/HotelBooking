package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.request.CreateBookingRequest;
import com.hotelbooking.hotel_booking.model.Booking;

public interface BookingService {

    Booking createBooking(CreateBookingRequest request);

    Booking getBooking(String bookingId);
}
