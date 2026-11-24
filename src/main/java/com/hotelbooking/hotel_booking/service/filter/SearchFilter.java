package com.hotelbooking.hotel_booking.service.filter;

import com.hotelbooking.hotel_booking.dto.request.SearchRequest;
import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.model.RoomType;

public interface SearchFilter {

    boolean matches(Property property, RoomType roomType, SearchRequest request);
}
