package com.hotelbooking.hotel_booking.model;

import com.hotelbooking.hotel_booking.util.Guard;
import lombok.Value;

@Value
public class Location {

    String addressLine;
    String locality;
    String city;

    public Location(String addressLine, String locality, String city) {
        this.addressLine = Guard.notBlank(addressLine, "addressLine");
        this.locality = Guard.notBlank(locality, "locality");
        this.city = Guard.notBlank(city, "city");
    }

    public boolean isInCity(String candidate) {
        return city.equalsIgnoreCase(candidate.strip());
    }

    public boolean isInLocality(String candidate) {
        return locality.equalsIgnoreCase(candidate.strip());
    }
}
