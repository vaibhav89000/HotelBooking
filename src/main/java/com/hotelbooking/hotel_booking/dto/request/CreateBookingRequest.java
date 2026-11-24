package com.hotelbooking.hotel_booking.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotBlank
    private String propertyId;

    @NotBlank
    private String roomTypeId;

    @NotNull
    @FutureOrPresent
    private LocalDate checkIn;

    @NotNull
    private LocalDate checkOut;

    @NotNull
    @Positive
    private Integer guests;
}
