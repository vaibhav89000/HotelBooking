package com.hotelbooking.hotel_booking.service.filter;

import com.hotelbooking.hotel_booking.dto.request.SearchRequest;
import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.model.RoomType;
import org.springframework.stereotype.Component;

@Component
public class StarRatingFilter implements SearchFilter {

    @Override
    public boolean matches(Property property, RoomType roomType, SearchRequest request) {
        return request.getMinStars() == null || property.getStarRating() >= request.getMinStars();
    }
}
