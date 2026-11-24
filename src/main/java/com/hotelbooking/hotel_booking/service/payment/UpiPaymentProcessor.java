package com.hotelbooking.hotel_booking.service.payment;

import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import org.springframework.stereotype.Component;

@Component
public class UpiPaymentProcessor extends AbstractGatewayPaymentProcessor {

    public UpiPaymentProcessor(PaymentGateway paymentGateway) {
        super(paymentGateway);
    }

    @Override
    public PaymentMethodType method() {
        return PaymentMethodType.UPI;
    }
}
