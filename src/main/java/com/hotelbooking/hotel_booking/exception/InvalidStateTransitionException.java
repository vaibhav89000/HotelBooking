package com.hotelbooking.hotel_booking.exception;

public class InvalidStateTransitionException extends DomainException {

    public InvalidStateTransitionException(Enum<?> from, Enum<?> to) {
        super("Cannot transition %s from %s to %s"
                .formatted(from.getDeclaringClass().getSimpleName(), from, to));
    }
}
