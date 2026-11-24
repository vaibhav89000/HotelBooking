package com.hotelbooking.hotel_booking.repository.inmemory;

import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.repository.PropertyRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InMemoryPropertyRepository extends InMemoryRepository<Property> implements PropertyRepository {

    public InMemoryPropertyRepository() {
        super(Property::getId);
    }

    @Override
    public List<Property> findByCity(String city) {
        return findWhere(property -> property.getLocation().isInCity(city));
    }
}
