package com.hotelbooking.hotel_booking.repository;

import java.util.Optional;

public interface BaseRepository<T> {

    T save(T aggregate);

    Optional<T> findById(String id);
}
