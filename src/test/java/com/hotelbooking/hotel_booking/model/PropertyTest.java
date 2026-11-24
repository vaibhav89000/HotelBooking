package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.enums.Amenity;
import com.hotelbooking.hotel_booking.exception.DomainValidationException;
import com.hotelbooking.hotel_booking.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyTest {

    private static final Location LOCATION = new Location("12 MG Road", "Indiranagar", "Bengaluru");
    private static final RoomType DELUXE = roomType("deluxe", 2);
    private static final RoomType SUITE = roomType("suite", 4);

    @Test
    void requiresAtLeastOneRoomType() {
        assertThatThrownBy(() -> property(List.of()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("roomTypes");
    }

    @Test
    void rejectsDuplicateRoomTypeIds() {
        assertThatThrownBy(() -> property(List.of(DELUXE, roomType("deluxe", 3))))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Duplicate room type");
    }

    @Test
    void starRatingMustBeBetweenOneAndFive() {
        assertThatThrownBy(() -> new Property("p-1", "owner-1", "Lakeview", LOCATION, 6, Set.of(), List.of(DELUXE)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("starRating");
    }

    @Test
    void looksUpRoomTypesById() {
        Property property = property(List.of(DELUXE, SUITE));

        assertThat(property.roomType("suite")).isSameAs(SUITE);
        assertThatThrownBy(() -> property.roomType("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void filtersRoomTypesByGuestCapacity() {
        Property property = property(List.of(DELUXE, SUITE));

        assertThat(property.roomTypesAccommodating(2)).containsExactly(DELUXE, SUITE);
        assertThat(property.roomTypesAccommodating(3)).containsExactly(SUITE);
        assertThat(property.roomTypesAccommodating(5)).isEmpty();
    }

    @Test
    void matchesRequiredAmenities() {
        Property property = property(List.of(DELUXE));

        assertThat(property.offersAll(Set.of(Amenity.WIFI))).isTrue();
        assertThat(property.offersAll(Set.of(Amenity.WIFI, Amenity.SPA))).isFalse();
    }

    @Test
    void exposesCollectionsAsReadOnly() {
        Property property = property(List.of(DELUXE));

        assertThatThrownBy(() -> property.getAmenities().add(Amenity.SPA))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> property.getRoomTypes().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static Property property(List<RoomType> roomTypes) {
        return new Property("p-1", "owner-1", "Lakeview", LOCATION, 4,
                Set.of(Amenity.WIFI, Amenity.POOL), roomTypes);
    }

    private static RoomType roomType(String id, int maxOccupancy) {
        return new RoomType(id, id.toUpperCase(), maxOccupancy, 5, new BigDecimal("3000"));
    }
}
