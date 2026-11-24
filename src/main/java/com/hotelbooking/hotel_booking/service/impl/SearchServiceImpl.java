package com.hotelbooking.hotel_booking.service.impl;

import com.hotelbooking.hotel_booking.dto.request.SearchRequest;
import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.model.SearchResult;
import com.hotelbooking.hotel_booking.repository.PropertyRepository;
import com.hotelbooking.hotel_booking.service.SearchService;
import com.hotelbooking.hotel_booking.service.filter.SearchFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final PropertyRepository propertyRepository;
    private final List<SearchFilter> filters;

    @Override
    public List<SearchResult> search(SearchRequest request) {
        return propertyRepository.findByCity(request.getCity()).stream()
                .map(property -> new SearchResult(property, matchingRoomTypes(property, request)))
                .filter(result -> !result.getRoomTypes().isEmpty())
                .toList();
    }

    private List<RoomType> matchingRoomTypes(Property property, SearchRequest request) {
        return property.getRoomTypes().stream()
                .filter(roomType -> matchesAllFilters(property, roomType, request))
                .toList();
    }

    private boolean matchesAllFilters(Property property, RoomType roomType, SearchRequest request) {
        return filters.stream().allMatch(filter -> filter.matches(property, roomType, request));
    }
}
