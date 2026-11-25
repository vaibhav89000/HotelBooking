package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.request.CreatePaymentRequest;
import com.hotelbooking.hotel_booking.dto.response.PaymentResponse;
import com.hotelbooking.hotel_booking.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings/{bookingId}/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Booking payment processing")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Pay for a booking", description = "Processes a payment using CARD, UPI, or WALLET. The Idempotency-Key header makes retries safe.")
    public PaymentResponse pay(@PathVariable String bookingId,
                               @Parameter(description = "Unique key for this payment attempt; reuse it to safely retry the same request.", required = true)
                               @RequestHeader("Idempotency-Key") String idempotencyKey,
                               @Valid @RequestBody CreatePaymentRequest request) {
        return PaymentResponse.from(
                paymentService.pay(bookingId, request.getMethod(), idempotencyKey));
    }
}
