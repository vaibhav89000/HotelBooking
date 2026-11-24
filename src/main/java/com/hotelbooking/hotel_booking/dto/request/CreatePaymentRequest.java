package com.hotelbooking.hotel_booking.dto.request;

import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class CreatePaymentRequest {

    @NotNull
    PaymentMethodType method;
}
