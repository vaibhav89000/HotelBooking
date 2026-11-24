package com.hotelbooking.hotel_booking.enums;

import com.hotelbooking.hotel_booking.model.LifecycleState;

import java.util.EnumSet;
import java.util.Set;

public enum PaymentStatus implements LifecycleState<PaymentStatus> {

    INITIATED,
    SUCCEEDED,
    FAILED,
    REFUNDED;

    @Override
    public Set<PaymentStatus> allowedTransitions() {
        return switch (this) {
            case INITIATED -> EnumSet.of(SUCCEEDED, FAILED);
            case SUCCEEDED -> EnumSet.of(REFUNDED);
            case FAILED, REFUNDED -> EnumSet.noneOf(PaymentStatus.class);
        };
    }
}
