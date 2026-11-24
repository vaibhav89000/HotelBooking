package com.hotelbooking.hotel_booking.enums;

import com.hotelbooking.hotel_booking.model.LifecycleState;

import java.util.EnumSet;
import java.util.Set;

public enum BookingStatus implements LifecycleState<BookingStatus> {

    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED;

    @Override
    public Set<BookingStatus> allowedTransitions() {
        return switch (this) {
            case PENDING_PAYMENT -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(CANCELLED);
            case CANCELLED -> EnumSet.noneOf(BookingStatus.class);
        };
    }
}
