package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    private static final LocalDate DAY = LocalDate.of(2026, 12, 10);

    @Test
    void checkOutMustBeAfterCheckIn() {
        assertThatThrownBy(() -> DateRange.of(DAY, DAY))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> DateRange.of(DAY, DAY.minusDays(1)))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void countsNightsExcludingCheckOutDay() {
        DateRange stay = DateRange.of(DAY, DAY.plusDays(3));

        assertThat(stay.nights()).isEqualTo(3);
        assertThat(stay.nightsStream()).containsExactly(DAY, DAY.plusDays(1), DAY.plusDays(2));
    }
}
