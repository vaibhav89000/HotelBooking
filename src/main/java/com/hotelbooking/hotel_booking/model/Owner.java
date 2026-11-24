package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.util.Guard;
import lombok.Getter;

@Getter
public class Owner {

    private final String id;
    private final String name;

    public Owner(String id, String name) {
        this.id = Guard.notBlank(id, "id");
        this.name = Guard.notBlank(name, "name");
    }
}
