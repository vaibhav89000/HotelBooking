package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.util.Guard;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class RoomType {

    private final String id;
    private final String name;
    private final int maxOccupancy;
    private final int totalRooms;
    private final BigDecimal basePricePerNight;

    public RoomType(String id, String name, int maxOccupancy, int totalRooms, BigDecimal basePricePerNight) {
        this.id = Guard.notBlank(id, "roomType.id");
        this.name = Guard.notBlank(name, "roomType.name");
        this.maxOccupancy = Guard.positive(maxOccupancy, "roomType.maxOccupancy");
        this.totalRooms = Guard.positive(totalRooms, "roomType.totalRooms");
        this.basePricePerNight = Guard.nonNegative(basePricePerNight, "roomType.basePricePerNight");
    }

    public boolean canAccommodate(int guests) {
        return guests > 0 && guests <= maxOccupancy;
    }
}
