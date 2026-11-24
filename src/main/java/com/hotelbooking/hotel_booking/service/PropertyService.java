package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.request.OnboardPropertyRequest;
import com.hotelbooking.hotel_booking.model.Property;

public interface PropertyService {

    Property onboardProperty(String ownerId, OnboardPropertyRequest request);

    Property getProperty(String propertyId);
}
