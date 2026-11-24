package com.hotelbooking.hotel_booking.repository.inmemory;

import com.hotelbooking.hotel_booking.model.RoomInventory;
import com.hotelbooking.hotel_booking.repository.RoomInventoryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryRoomInventoryRepository extends InMemoryRepository<RoomInventory>
        implements RoomInventoryRepository {

    public InMemoryRoomInventoryRepository() {
        super(RoomInventory::getRoomTypeId);
    }
}
