package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.request.CreateBookingRequest;
import com.hotelbooking.hotel_booking.dto.response.BookingResponse;
import com.hotelbooking.hotel_booking.dto.response.CancellationResponse;
import com.hotelbooking.hotel_booking.service.BookingService;
import com.hotelbooking.hotel_booking.service.CancellationService;
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

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final CancellationService cancellationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return BookingResponse.from(bookingService.createBooking(request));
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable String bookingId) {
        return BookingResponse.from(bookingService.getBooking(bookingId));
    }

    @PostMapping("/{bookingId}/cancel")
    public CancellationResponse cancelBooking(@PathVariable String bookingId) {
        return CancellationResponse.from(cancellationService.cancel(bookingId));
    }
}
