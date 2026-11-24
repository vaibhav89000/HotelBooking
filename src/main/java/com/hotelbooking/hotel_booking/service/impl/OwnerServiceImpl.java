package com.hotelbooking.hotel_booking.service.impl;

import com.hotelbooking.hotel_booking.dto.request.CreateOwnerRequest;
import com.hotelbooking.hotel_booking.exception.ResourceNotFoundException;
import com.hotelbooking.hotel_booking.model.Owner;
import com.hotelbooking.hotel_booking.repository.OwnerRepository;
import com.hotelbooking.hotel_booking.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OwnerServiceImpl implements OwnerService {

    private final OwnerRepository ownerRepository;

    @Override
    public Owner createOwner(CreateOwnerRequest request) {
        return ownerRepository.save(new Owner(UUID.randomUUID().toString(), request.getName()));
    }

    @Override
    public Owner getOwner(String ownerId) {
        return ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner", ownerId));
    }
}
