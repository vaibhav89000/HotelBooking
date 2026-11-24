package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.enums.Amenity;
import com.hotelbooking.hotel_booking.exception.DomainValidationException;
import com.hotelbooking.hotel_booking.exception.ResourceNotFoundException;
import com.hotelbooking.hotel_booking.util.Guard;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class Property {

    private final String id;
    private final String ownerId;
    private final String name;
    private final Location location;
    private final int starRating;
    private final Set<Amenity> amenities;
    private final Map<String, RoomType> roomTypes = new LinkedHashMap<>();

    public Property(String id, String ownerId, String name, Location location, int starRating,
                    Set<Amenity> amenities, List<RoomType> roomTypes) {
        Guard.check(starRating >= 1 && starRating <= 5, "starRating must be between 1 and 5");
        this.id = Guard.notBlank(id, "id");
        this.ownerId = Guard.notBlank(ownerId, "ownerId");
        this.name = Guard.notBlank(name, "name");
        this.location = Guard.notNull(location, "location");
        this.starRating = starRating;
        this.amenities = amenities == null || amenities.isEmpty()
                ? EnumSet.noneOf(Amenity.class)
                : EnumSet.copyOf(amenities);
        Guard.notEmpty(roomTypes, "roomTypes").forEach(this::addRoomType);
    }

    public RoomType roomType(String roomTypeId) {
        RoomType roomType = roomTypes.get(roomTypeId);
        if (roomType == null) {
            throw new ResourceNotFoundException("RoomType", roomTypeId);
        }
        return roomType;
    }

    public Collection<RoomType> getRoomTypes() {
        return Collections.unmodifiableCollection(roomTypes.values());
    }

    public Set<Amenity> getAmenities() {
        return Collections.unmodifiableSet(amenities);
    }

    public boolean offersAll(Set<Amenity> required) {
        return amenities.containsAll(required);
    }

    public List<RoomType> roomTypesAccommodating(int guests) {
        return roomTypes.values().stream()
                .filter(roomType -> roomType.canAccommodate(guests))
                .toList();
    }

    private void addRoomType(RoomType roomType) {
        if (roomTypes.putIfAbsent(roomType.getId(), roomType) != null) {
            throw new DomainValidationException("Duplicate room type id: " + roomType.getId());
        }
    }
}
