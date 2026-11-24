package com.hotelbooking.hotel_booking.exception;

import com.hotelbooking.hotel_booking.model.DateRange;

public class RoomUnavailableException extends DomainException {

    public RoomUnavailableException(String roomTypeId, DateRange stay) {
        super("Room type %s is not available for %s".formatted(roomTypeId, stay));
    }
}
