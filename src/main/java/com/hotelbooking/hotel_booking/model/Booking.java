package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.util.Guard;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Booking {

    private final String id;
    private final String propertyId;
    private final String roomTypeId;
    private final DateRange stay;
    private final int guestCount;
    private final BigDecimal totalAmount;
    private BookingStatus status;

    public Booking(String id, String propertyId, String roomTypeId, DateRange stay, int guestCount,
                   BigDecimal totalAmount) {
        this.id = Guard.notBlank(id, "id");
        this.propertyId = Guard.notBlank(propertyId, "propertyId");
        this.roomTypeId = Guard.notBlank(roomTypeId, "roomTypeId");
        this.stay = Guard.notNull(stay, "stay");
        this.guestCount = Guard.positive(guestCount, "guestCount");
        this.totalAmount = Guard.nonNegative(totalAmount, "totalAmount");
        this.status = BookingStatus.PENDING_PAYMENT;
    }

    public void confirm() {
        status = status.transitionTo(BookingStatus.CONFIRMED);
    }

    public void cancel() {
        status = status.transitionTo(BookingStatus.CANCELLED);
    }

    public boolean isConfirmed() {
        return status == BookingStatus.CONFIRMED;
    }
}
