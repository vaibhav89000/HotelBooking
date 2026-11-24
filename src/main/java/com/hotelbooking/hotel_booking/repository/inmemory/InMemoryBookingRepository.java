package com.hotelbooking.hotel_booking.repository.inmemory;

import com.hotelbooking.hotel_booking.model.Booking;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryBookingRepository extends InMemoryRepository<Booking> implements BookingRepository {

    public InMemoryBookingRepository() {
        super(Booking::getId);
    }
}
