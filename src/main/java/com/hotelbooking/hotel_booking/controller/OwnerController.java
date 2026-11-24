package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.request.CreateOwnerRequest;
import com.hotelbooking.hotel_booking.dto.response.OwnerResponse;
import com.hotelbooking.hotel_booking.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OwnerResponse createOwner(@Valid @RequestBody CreateOwnerRequest request) {
        return OwnerResponse.from(ownerService.createOwner(request));
    }
}
