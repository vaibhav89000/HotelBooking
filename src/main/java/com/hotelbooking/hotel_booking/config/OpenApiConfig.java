package com.hotelbooking.hotel_booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hotelBookingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hotel Booking Service API")
                        .description("""
                                REST API for hotel owner onboarding, property discovery,
                                room availability, booking lifecycle, payments, and cancellations.
                                The current implementation uses in-memory persistence and a mock payment gateway.
                                """)
                        .version("v1")
                        .contact(new Contact()
                                .name("Hotel Booking API")
                                .url("https://github.com/vaibhav89000/HotelBooking")))
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Local development server")));
    }
}
