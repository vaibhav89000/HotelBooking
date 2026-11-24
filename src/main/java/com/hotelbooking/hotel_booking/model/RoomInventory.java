package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.exception.DomainException;
import com.hotelbooking.hotel_booking.exception.RoomUnavailableException;
import com.hotelbooking.hotel_booking.util.Guard;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class RoomInventory {

    @Getter
    private final String roomTypeId;
    @Getter
    private final int totalRooms;
    private final Map<LocalDate, Integer> reservedByNight = new HashMap<>();

    public RoomInventory(String roomTypeId, int totalRooms) {
        this.roomTypeId = Guard.notBlank(roomTypeId, "roomTypeId");
        this.totalRooms = Guard.positive(totalRooms, "totalRooms");
    }

    public int availableOn(LocalDate night) {
        return totalRooms - reservedOn(night);
    }

    public boolean isAvailable(DateRange stay) {
        return stay.nightsStream().allMatch(night -> availableOn(night) > 0);
    }

    public void reserve(DateRange stay) {
        if (!isAvailable(stay)) {
            throw new RoomUnavailableException(roomTypeId, stay);
        }
        stay.nightsStream().forEach(night -> reservedByNight.merge(night, 1, Integer::sum));
    }

    public void release(DateRange stay) {
        if (stay.nightsStream().anyMatch(night -> reservedOn(night) == 0)) {
            throw new DomainException("Cannot release unreserved nights %s for room type %s"
                    .formatted(stay, roomTypeId));
        }
        stay.nightsStream().forEach(night -> reservedByNight.computeIfPresent(night,
                (date, reserved) -> reserved == 1 ? null : reserved - 1));
    }

    private int reservedOn(LocalDate night) {
        return reservedByNight.getOrDefault(night, 0);
    }
}
