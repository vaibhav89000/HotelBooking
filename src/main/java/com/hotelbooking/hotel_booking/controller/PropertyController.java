package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.request.OnboardPropertyRequest;
import com.hotelbooking.hotel_booking.dto.request.SearchRequest;
import com.hotelbooking.hotel_booking.dto.response.PropertyResponse;
import com.hotelbooking.hotel_booking.dto.response.SearchResultResponse;
import com.hotelbooking.hotel_booking.service.PropertyService;
import com.hotelbooking.hotel_booking.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Properties", description = "Hotel property onboarding, retrieval, and search")
public class PropertyController {

    private final PropertyService propertyService;
    private final SearchService searchService;

    @PostMapping("/owners/{ownerId}/properties")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Onboard a property", description = "Creates a property and its room types under the specified owner.")
    public PropertyResponse onboardProperty(@PathVariable String ownerId,
                                            @Valid @RequestBody OnboardPropertyRequest request) {
        return PropertyResponse.from(propertyService.onboardProperty(ownerId, request));
    }

    @GetMapping("/properties/{propertyId}")
    @Operation(summary = "Get property details", description = "Returns property information and its room types.")
    public PropertyResponse getProperty(@PathVariable String propertyId) {
        return PropertyResponse.from(propertyService.getProperty(propertyId));
    }

    @GetMapping("/properties/search")
    @Operation(summary = "Search available properties", description = "Searches properties by city, stay dates, guest count, and optional locality, price, amenities, and star-rating filters.")
    public List<SearchResultResponse> search(@Valid SearchRequest request) {
        return searchService.search(request).stream()
                .map(result -> SearchResultResponse.from(result, request.stay()))
                .toList();
    }
}
