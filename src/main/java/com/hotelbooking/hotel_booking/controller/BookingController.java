package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.request.CreateBookingRequest;
import com.hotelbooking.hotel_booking.dto.response.BookingResponse;
import com.hotelbooking.hotel_booking.dto.response.CancellationResponse;
import com.hotelbooking.hotel_booking.model.CancellationOutcome;
import com.hotelbooking.hotel_booking.service.BookingService;
import com.hotelbooking.hotel_booking.service.CancellationService;
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

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Create, retrieve, and cancel hotel bookings")
public class BookingController {

    private final BookingService bookingService;
    private final CancellationService cancellationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a booking", description = "Reserves room inventory for the requested stay. New bookings start in PENDING_PAYMENT.")
    public BookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return BookingResponse.from(bookingService.createBooking(request));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking details", description = "Returns the current state and details of a booking.")
    public BookingResponse getBooking(@PathVariable String bookingId) {
        return BookingResponse.from(bookingService.getBooking(bookingId));
    }

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel a booking", description = "Cancels a booking, applies the cancellation policy, issues an eligible refund, and releases room inventory. Repeated calls are safe.")
    public CancellationResponse cancelBooking(@PathVariable String bookingId) {
        CancellationOutcome outcome = cancellationService.cancel(bookingId);
        return CancellationResponse.from(outcome.booking(), outcome.payment(), outcome.refundAmount());
    }
}
