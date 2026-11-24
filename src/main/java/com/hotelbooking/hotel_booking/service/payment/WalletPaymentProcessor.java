package com.hotelbooking.hotel_booking.service.payment;

import com.hotelbooking.hotel_booking.enums.PaymentMethodType;
import org.springframework.stereotype.Component;

@Component
public class WalletPaymentProcessor extends AbstractGatewayPaymentProcessor {

    public WalletPaymentProcessor(PaymentGateway paymentGateway) {
        super(paymentGateway);
    }

    @Override
    public PaymentMethodType method() {
        return PaymentMethodType.WALLET;
    }
}
