package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.request.CreateOwnerRequest;
import com.hotelbooking.hotel_booking.model.Owner;

public interface OwnerService {

    Owner createOwner(CreateOwnerRequest request);

    Owner getOwner(String ownerId);
}
