package com.hotelbooking.hotel_booking.repository.inmemory;

import com.hotelbooking.hotel_booking.model.Owner;
import com.hotelbooking.hotel_booking.repository.OwnerRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOwnerRepository extends InMemoryRepository<Owner> implements OwnerRepository {

    public InMemoryOwnerRepository() {
        super(Owner::getId);
    }
}
