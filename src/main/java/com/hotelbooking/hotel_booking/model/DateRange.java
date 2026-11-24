package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.util.Guard;
import lombok.Value;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@Value
public class DateRange {

    LocalDate checkIn;
    LocalDate checkOut;

    public DateRange(LocalDate checkIn, LocalDate checkOut) {
        Guard.notNull(checkIn, "checkIn");
        Guard.notNull(checkOut, "checkOut");
        Guard.check(checkOut.isAfter(checkIn), "checkOut must be after checkIn");
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public static DateRange of(LocalDate checkIn, LocalDate checkOut) {
        return new DateRange(checkIn, checkOut);
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public Stream<LocalDate> nightsStream() {
        return checkIn.datesUntil(checkOut);
    }

    public boolean startsBefore(LocalDate date) {
        return checkIn.isBefore(date);
    }

    @Override
    public String toString() {
        return "[" + checkIn + " -> " + checkOut + ")";
    }
}
