package com.hotelbooking.hotel_booking.enums;

import com.hotelbooking.hotel_booking.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingStatusTest {

    @ParameterizedTest
    @CsvSource({
            "PENDING_PAYMENT, CONFIRMED, true",
            "PENDING_PAYMENT, CANCELLED, true",
            "CONFIRMED, CANCELLED, true",
            "CONFIRMED, PENDING_PAYMENT, false",
            "CANCELLED, CONFIRMED, false",
            "CANCELLED, PENDING_PAYMENT, false"
    })
    void definesTheBookingLifecycle(BookingStatus from, BookingStatus to, boolean allowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(allowed);
    }

    @Test
    void illegalTransitionThrows() {
        assertThatThrownBy(() -> BookingStatus.CANCELLED.transitionTo(BookingStatus.CONFIRMED))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessage("Cannot transition BookingStatus from CANCELLED to CONFIRMED");
    }
}
