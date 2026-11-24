package com.hotelbooking.hotel_booking.service.impl;

import com.hotelbooking.hotel_booking.dto.request.OnboardPropertyRequest.RoomTypeRequest;
import com.hotelbooking.hotel_booking.dto.request.OnboardPropertyRequest;
import com.hotelbooking.hotel_booking.exception.ResourceNotFoundException;
import com.hotelbooking.hotel_booking.model.Location;
import com.hotelbooking.hotel_booking.model.Owner;
import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.model.RoomInventory;
import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.repository.PropertyRepository;
import com.hotelbooking.hotel_booking.repository.RoomInventoryRepository;
import com.hotelbooking.hotel_booking.service.OwnerService;
import com.hotelbooking.hotel_booking.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final OwnerService ownerService;
    private final PropertyRepository propertyRepository;
    private final RoomInventoryRepository inventoryRepository;

    @Override
    public Property onboardProperty(String ownerId, OnboardPropertyRequest request) {
        Owner owner = ownerService.getOwner(ownerId);

        Property property = new Property(
                newId(),
                owner.getId(),
                request.getName(),
                new Location(request.getAddressLine(), request.getLocality(), request.getCity()),
                request.getStarRating(),
                request.getAmenities(),
                toRoomTypes(request.getRoomTypes()));

        openInventory(property);
        return propertyRepository.save(property);
    }

    @Override
    public Property getProperty(String propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));
    }

    private List<RoomType> toRoomTypes(List<RoomTypeRequest> requests) {
        return requests.stream()
                .map(room -> new RoomType(newId(), room.getName(), room.getMaxOccupancy(), room.getTotalRooms(),
                        room.getPricePerNight()))
                .toList();
    }

    private void openInventory(Property property) {
        for (RoomType roomType : property.getRoomTypes()) {
            inventoryRepository.save(new RoomInventory(roomType.getId(), roomType.getTotalRooms()));
        }
    }

    private static String newId() {
        return UUID.randomUUID().toString();
    }
}
