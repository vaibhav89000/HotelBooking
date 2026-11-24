package com.hotelbooking.hotel_booking.service.filter;

import com.hotelbooking.hotel_booking.dto.request.SearchRequest;
import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.repository.RoomInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvailabilityFilter implements SearchFilter {

    private final RoomInventoryRepository inventoryRepository;

    @Override
    public boolean matches(Property property, RoomType roomType, SearchRequest request) {
        return inventoryRepository.findById(roomType.getId())
                .map(inventory -> inventory.isAvailable(request.stay()))
                .orElse(false);
    }
}
