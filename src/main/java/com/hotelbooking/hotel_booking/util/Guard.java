package com.hotelbooking.hotel_booking.util;

import com.hotelbooking.hotel_booking.exception.DomainValidationException;

import java.math.BigDecimal;
import java.util.Collection;

public final class Guard {

    private Guard() {
    }

    public static <T> T notNull(T value, String field) {
        if (value == null) {
            throw new DomainValidationException(field + " must not be null");
        }
        return value;
    }

    public static String notBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(field + " must not be blank");
        }
        return value.strip();
    }

    public static int positive(int value, String field) {
        if (value <= 0) {
            throw new DomainValidationException(field + " must be positive");
        }
        return value;
    }

    public static BigDecimal nonNegative(BigDecimal value, String field) {
        notNull(value, field);
        if (value.signum() < 0) {
            throw new DomainValidationException(field + " must not be negative");
        }
        return value;
    }

    public static <C extends Collection<?>> C notEmpty(C values, String field) {
        if (values == null || values.isEmpty()) {
            throw new DomainValidationException(field + " must not be empty");
        }
        return values;
    }

    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new DomainValidationException(message);
        }
    }
}
