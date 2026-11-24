package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.exception.DomainValidationException;
import com.hotelbooking.hotel_booking.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingTest {

    @Test
    void newBookingAwaitsPayment() {
        assertThat(newBooking(2).getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void confirmedBookingCanBeCancelled() {
        Booking booking = newBooking(2);

        booking.confirm();
        booking.cancel();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelledBookingCannotBeConfirmed() {
        Booking booking = newBooking(2);
        booking.cancel();

        assertThatThrownBy(booking::confirm).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cannotConfirmTwice() {
        Booking booking = newBooking(2);
        booking.confirm();

        assertThatThrownBy(booking::confirm).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void rejectsNonPositiveGuestCount() {
        assertThatThrownBy(() -> newBooking(0)).isInstanceOf(DomainValidationException.class);
    }

    private static Booking newBooking(int guests) {
        LocalDate checkIn = LocalDate.of(2026, 12, 10);
        return new Booking("b-1", "p-1", "deluxe",
                DateRange.of(checkIn, checkIn.plusDays(2)), guests, new BigDecimal("6000"));
    }
}
