package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.exception.InvalidStateTransitionException;

import java.util.Set;

public interface LifecycleState<S extends Enum<S> & LifecycleState<S>> {

    Set<S> allowedTransitions();

    default boolean canTransitionTo(S target) {
        return allowedTransitions().contains(target);
    }

    @SuppressWarnings("unchecked")
    default S transitionTo(S target) {
        if (!canTransitionTo(target)) {
            throw new InvalidStateTransitionException((S) this, target);
        }
        return target;
    }
}
