package com.hotelbooking.hotel_booking.service.filter;

import com.hotelbooking.hotel_booking.dto.request.SearchRequest;
import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.model.RoomType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PriceRangeFilter implements SearchFilter {

    @Override
    public boolean matches(Property property, RoomType roomType, SearchRequest request) {
        BigDecimal price = roomType.getBasePricePerNight();
        boolean aboveMin = request.getMinPrice() == null || price.compareTo(request.getMinPrice()) >= 0;
        boolean belowMax = request.getMaxPrice() == null || price.compareTo(request.getMaxPrice()) <= 0;
        return aboveMin && belowMax;
    }
}
