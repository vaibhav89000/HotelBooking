package com.hotelbooking.hotel_booking.service.impl;

import com.hotelbooking.hotel_booking.dto.request.CreateBookingRequest;
import com.hotelbooking.hotel_booking.exception.DomainValidationException;
import com.hotelbooking.hotel_booking.exception.ResourceNotFoundException;
import com.hotelbooking.hotel_booking.lock.LockManager;
import com.hotelbooking.hotel_booking.model.Booking;
import com.hotelbooking.hotel_booking.model.DateRange;
import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.model.RoomInventory;
import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import com.hotelbooking.hotel_booking.repository.RoomInventoryRepository;
import com.hotelbooking.hotel_booking.service.BookingService;
import com.hotelbooking.hotel_booking.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final PropertyService propertyService;
    private final BookingRepository bookingRepository;
    private final RoomInventoryRepository inventoryRepository;
    private final LockManager lockManager;

    @Override
    public Booking createBooking(CreateBookingRequest request) {
        Property property = propertyService.getProperty(request.getPropertyId());
        RoomType roomType = property.roomType(request.getRoomTypeId());
        DateRange stay = DateRange.of(request.getCheckIn(), request.getCheckOut());
        checkCapacity(roomType, request.getGuests());

        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                property.getId(),
                roomType.getId(),
                stay,
                request.getGuests(),
                roomType.priceFor(stay));

        return lockManager.withLock(roomType.getId(), () -> {
            inventoryOf(roomType.getId()).reserve(stay);
            return bookingRepository.save(booking);
        });
    }

    @Override
    public Booking getBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    private void checkCapacity(RoomType roomType, int guests) {
        if (!roomType.canAccommodate(guests)) {
            throw new DomainValidationException("%s allows at most %d guests"
                    .formatted(roomType.getName(), roomType.getMaxOccupancy()));
        }
    }

    private RoomInventory inventoryOf(String roomTypeId) {
        return inventoryRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("RoomInventory", roomTypeId));
    }
}
