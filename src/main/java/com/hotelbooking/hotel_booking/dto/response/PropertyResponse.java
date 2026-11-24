package com.hotelbooking.hotel_booking.dto.response;

import com.hotelbooking.hotel_booking.enums.Amenity;
import com.hotelbooking.hotel_booking.model.Location;
import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.model.RoomType;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Value
public class PropertyResponse {

    String id;
    String ownerId;
    String name;
    Location location;
    int starRating;
    Set<Amenity> amenities;
    List<RoomTypeResponse> roomTypes;

    public static PropertyResponse from(Property property) {
        return new PropertyResponse(
                property.getId(),
                property.getOwnerId(),
                property.getName(),
                property.getLocation(),
                property.getStarRating(),
                property.getAmenities(),
                property.getRoomTypes().stream().map(RoomTypeResponse::from).toList());
    }

    @Value
    public static class RoomTypeResponse {

        String id;
        String name;
        int maxOccupancy;
        int totalRooms;
        BigDecimal pricePerNight;

        static RoomTypeResponse from(RoomType roomType) {
            return new RoomTypeResponse(roomType.getId(), roomType.getName(), roomType.getMaxOccupancy(),
                    roomType.getTotalRooms(), roomType.getBasePricePerNight());
        }
    }
}
