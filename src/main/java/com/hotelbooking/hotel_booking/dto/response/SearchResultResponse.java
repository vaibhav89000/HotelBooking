package com.hotelbooking.hotel_booking.dto.response;

import com.hotelbooking.hotel_booking.enums.Amenity;
import com.hotelbooking.hotel_booking.model.DateRange;
import com.hotelbooking.hotel_booking.model.Location;
import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.model.SearchResult;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Value
public class SearchResultResponse {

    String propertyId;
    String name;
    Location location;
    int starRating;
    Set<Amenity> amenities;
    List<AvailableRoomResponse> availableRooms;

    public static SearchResultResponse from(SearchResult result, DateRange stay) {
        Property property = result.getProperty();
        return new SearchResultResponse(
                property.getId(),
                property.getName(),
                property.getLocation(),
                property.getStarRating(),
                property.getAmenities(),
                result.getRoomTypes().stream().map(roomType -> AvailableRoomResponse.from(roomType, stay)).toList());
    }

    @Value
    public static class AvailableRoomResponse {

        String roomTypeId;
        String name;
        int maxOccupancy;
        BigDecimal pricePerNight;
        BigDecimal totalPrice;

        static AvailableRoomResponse from(RoomType roomType, DateRange stay) {
            return new AvailableRoomResponse(roomType.getId(), roomType.getName(), roomType.getMaxOccupancy(),
                    roomType.getBasePricePerNight(), roomType.priceFor(stay));
        }
    }
}
