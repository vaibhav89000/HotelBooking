package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.request.OnboardPropertyRequest;
import com.hotelbooking.hotel_booking.dto.request.SearchRequest;
import com.hotelbooking.hotel_booking.dto.response.PropertyResponse;
import com.hotelbooking.hotel_booking.dto.response.SearchResultResponse;
import com.hotelbooking.hotel_booking.service.PropertyService;
import com.hotelbooking.hotel_booking.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final SearchService searchService;

    @PostMapping("/owners/{ownerId}/properties")
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponse onboardProperty(@PathVariable String ownerId,
                                            @Valid @RequestBody OnboardPropertyRequest request) {
        return PropertyResponse.from(propertyService.onboardProperty(ownerId, request));
    }

    @GetMapping("/properties/{propertyId}")
    public PropertyResponse getProperty(@PathVariable String propertyId) {
        return PropertyResponse.from(propertyService.getProperty(propertyId));
    }

    @GetMapping("/properties/search")
    public List<SearchResultResponse> search(@Valid SearchRequest request) {
        return searchService.search(request).stream()
                .map(result -> SearchResultResponse.from(result, request.stay()))
                .toList();
    }
}
