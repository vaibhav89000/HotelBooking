package com.hotelbooking.hotel_booking.dto.response;

import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.model.Booking;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class BookingResponse {

    String id;
    String propertyId;
    String roomTypeId;
    LocalDate checkIn;
    LocalDate checkOut;
    int guests;
    BigDecimal totalAmount;
    BookingStatus status;

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getPropertyId(),
                booking.getRoomTypeId(),
                booking.getStay().getCheckIn(),
                booking.getStay().getCheckOut(),
                booking.getGuestCount(),
                booking.getTotalAmount(),
                booking.getStatus());
    }
}
