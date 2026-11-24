package com.hotelbooking.hotel_booking.repository;

import com.hotelbooking.hotel_booking.model.Property;

import java.util.List;

public interface PropertyRepository extends BaseRepository<Property> {

    List<Property> findAll();

    List<Property> findByCity(String city);
}
