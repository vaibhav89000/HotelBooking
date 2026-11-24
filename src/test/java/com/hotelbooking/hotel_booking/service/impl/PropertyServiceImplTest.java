package com.hotelbooking.hotel_booking.service.impl;

import com.hotelbooking.hotel_booking.dto.request.CreateOwnerRequest;
import com.hotelbooking.hotel_booking.dto.request.OnboardPropertyRequest.RoomTypeRequest;
import com.hotelbooking.hotel_booking.dto.request.OnboardPropertyRequest;
import com.hotelbooking.hotel_booking.enums.Amenity;
import com.hotelbooking.hotel_booking.exception.DomainValidationException;
import com.hotelbooking.hotel_booking.exception.ResourceNotFoundException;
import com.hotelbooking.hotel_booking.model.Owner;
import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.model.RoomInventory;
import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryOwnerRepository;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryPropertyRepository;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryRoomInventoryRepository;
import com.hotelbooking.hotel_booking.service.OwnerService;
import com.hotelbooking.hotel_booking.service.PropertyService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyServiceImplTest {

    private final InMemoryPropertyRepository propertyRepository = new InMemoryPropertyRepository();
    private final InMemoryRoomInventoryRepository inventoryRepository = new InMemoryRoomInventoryRepository();
    private final OwnerService ownerService = new OwnerServiceImpl(new InMemoryOwnerRepository());
    private final PropertyService propertyService =
            new PropertyServiceImpl(ownerService, propertyRepository, inventoryRepository);

    @Test
    void onboardsPropertyWithItsRoomTypes() {
        Owner owner = createOwner("Lakeview Stays");

        Property property = propertyService.onboardProperty(owner.getId(), lakeview(4));

        assertThat(property.getOwnerId()).isEqualTo(owner.getId());
        assertThat(property.getRoomTypes()).extracting(RoomType::getName).containsExactly("Deluxe", "Suite");
        assertThat(propertyService.getProperty(property.getId())).isSameAs(property);
    }

    @Test
    void opensInventoryForEveryRoomType() {
        Property property = propertyService.onboardProperty(createOwner("Lakeview Stays").getId(), lakeview(4));

        LocalDate anyNight = LocalDate.of(2026, 12, 10);
        for (RoomType roomType : property.getRoomTypes()) {
            RoomInventory inventory = inventoryRepository.findById(roomType.getId()).orElseThrow();
            assertThat(inventory.availableOn(anyNight)).isEqualTo(roomType.getTotalRooms());
        }
    }

    @Test
    void oneOwnerCanOnboardSeveralProperties() {
        Owner chain = createOwner("Grand Hotels Group");

        Property first = propertyService.onboardProperty(chain.getId(), lakeview(4));
        Property second = propertyService.onboardProperty(chain.getId(), lakeview(5));

        assertThat(List.of(first, second)).allMatch(property -> property.getOwnerId().equals(chain.getId()));
        assertThat(propertyRepository.findAll()).hasSize(2);
    }

    @Test
    void rejectsOnboardingForUnknownOwner() {
        assertThatThrownBy(() -> propertyService.onboardProperty("missing", lakeview(4)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Owner");
        assertThat(propertyRepository.findAll()).isEmpty();
    }

    @Test
    void invalidPropertyLeavesNoInventoryBehind() {
        Owner owner = createOwner("Lakeview Stays");

        assertThatThrownBy(() -> propertyService.onboardProperty(owner.getId(), lakeview(7)))
                .isInstanceOf(DomainValidationException.class);
        assertThat(inventoryRepository.findAll()).isEmpty();
    }

    @Test
    void unknownPropertyIsNotFound() {
        assertThatThrownBy(() -> propertyService.getProperty("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Owner createOwner(String name) {
        return ownerService.createOwner(new CreateOwnerRequest(name));
    }

    private static OnboardPropertyRequest lakeview(int starRating) {
        return new OnboardPropertyRequest("Lakeview", "12 MG Road", "Indiranagar", "Bengaluru", starRating,
                Set.of(Amenity.WIFI, Amenity.POOL),
                List.of(new RoomTypeRequest("Deluxe", 2, 10, new BigDecimal("3500")),
                        new RoomTypeRequest("Suite", 4, 2, new BigDecimal("8000"))));
    }
}
