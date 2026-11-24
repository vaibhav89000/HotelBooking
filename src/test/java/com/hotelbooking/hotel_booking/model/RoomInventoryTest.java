package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.exception.DomainException;
import com.hotelbooking.hotel_booking.exception.RoomUnavailableException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomInventoryTest {

    private static final LocalDate DAY = LocalDate.of(2026, 12, 10);
    private static final String ROOM_TYPE_ID = "deluxe";

    @Test
    void reservingConsumesOneRoomPerNight() {
        RoomInventory inventory = new RoomInventory(ROOM_TYPE_ID, 2);

        inventory.reserve(stay(0, 2));

        assertThat(inventory.availableOn(DAY)).isEqualTo(1);
        assertThat(inventory.availableOn(DAY.plusDays(1))).isEqualTo(1);
        assertThat(inventory.availableOn(DAY.plusDays(2))).isEqualTo(2);
    }

    @Test
    void preventsOverbookingOnOverlappingNights() {
        RoomInventory inventory = new RoomInventory(ROOM_TYPE_ID, 1);
        inventory.reserve(stay(0, 3));

        assertThatThrownBy(() -> inventory.reserve(stay(2, 4)))
                .isInstanceOf(RoomUnavailableException.class);
    }

    @Test
    void failedReservationLeavesInventoryUntouched() {
        RoomInventory inventory = new RoomInventory(ROOM_TYPE_ID, 1);
        inventory.reserve(stay(2, 3));

        assertThatThrownBy(() -> inventory.reserve(stay(0, 3)))
                .isInstanceOf(RoomUnavailableException.class);

        assertThat(inventory.availableOn(DAY)).isEqualTo(1);
        assertThat(inventory.availableOn(DAY.plusDays(1))).isEqualTo(1);
    }

    @Test
    void allowsBackToBackStaysOnTheLastRoom() {
        RoomInventory inventory = new RoomInventory(ROOM_TYPE_ID, 1);
        inventory.reserve(stay(0, 2));

        inventory.reserve(stay(2, 4));

        assertThat(inventory.isAvailable(stay(0, 4))).isFalse();
    }

    @Test
    void releaseMakesNightsBookableAgain() {
        RoomInventory inventory = new RoomInventory(ROOM_TYPE_ID, 1);
        inventory.reserve(stay(0, 2));

        inventory.release(stay(0, 2));

        assertThat(inventory.isAvailable(stay(0, 2))).isTrue();
    }

    @Test
    void releasingUnreservedNightsIsRejectedAtomically() {
        RoomInventory inventory = new RoomInventory(ROOM_TYPE_ID, 2);
        inventory.reserve(stay(0, 1));

        assertThatThrownBy(() -> inventory.release(stay(0, 2)))
                .isInstanceOf(DomainException.class);

        assertThat(inventory.availableOn(DAY)).isEqualTo(1);
    }

    private static DateRange stay(int fromOffset, int toOffset) {
        return DateRange.of(DAY.plusDays(fromOffset), DAY.plusDays(toOffset));
    }
}
