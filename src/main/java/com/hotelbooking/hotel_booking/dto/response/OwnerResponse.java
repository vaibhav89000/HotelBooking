package com.hotelbooking.hotel_booking.dto.response;

import com.hotelbooking.hotel_booking.model.Owner;
import lombok.Value;

@Value
public class OwnerResponse {

    String id;
    String name;

    public static OwnerResponse from(Owner owner) {
        return new OwnerResponse(owner.getId(), owner.getName());
    }
}
