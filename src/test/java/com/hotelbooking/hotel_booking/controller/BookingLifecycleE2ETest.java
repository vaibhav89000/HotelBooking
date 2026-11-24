package com.hotelbooking.hotel_booking.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the public HTTP API across owner onboarding, booking, payment,
 * cancellation/refund, and inventory becoming available for a new booking.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BookingLifecycleE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void completesBookingPaymentCancellationAndInventoryReleaseFlow() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(2);

        String ownerResponse = postCreated("/api/owners", """
                { "name": "Lifecycle Test Hotels" }
                """);
        String ownerId = JsonPath.read(ownerResponse, "$.id").toString();

        String propertyResponse = postCreated("/api/owners/" + ownerId + "/properties", """
                {
                  "name": "Lifecycle Hotel",
                  "addressLine": "12 MG Road",
                  "locality": "Indiranagar",
                  "city": "Bengaluru",
                  "starRating": 4,
                  "roomTypes": [
                    { "name": "Deluxe", "maxOccupancy": 2, "totalRooms": 1, "pricePerNight": 3500 }
                  ]
                }
                """);
        String propertyId = JsonPath.read(propertyResponse, "$.id").toString();
        String roomTypeId = JsonPath.read(propertyResponse, "$.roomTypes[0].id").toString();

        String bookingResponse = postCreated("/api/bookings", bookingJson(
                propertyId, roomTypeId, checkIn, checkOut));
        String bookingId = JsonPath.read(bookingResponse, "$.id").toString();

        mockMvc.perform(post("/api/bookings/{bookingId}/payments", bookingId)
                        .header("Idempotency-Key", "e2e-payment-" + bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "method": "UPI" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(bookingId))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));

        mockMvc.perform(get("/api/bookings/{bookingId}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(post("/api/bookings/{bookingId}/cancel", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(bookingId))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.refundAmount").value(7000));

        mockMvc.perform(post("/api/bookings/{bookingId}/cancel", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.refundAmount").value(7000));

        // The cancelled stay must be bookable again after inventory is released.
        MvcResult replacementBooking = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(propertyId, roomTypeId, checkIn, checkOut)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andReturn();

        String replacementId = JsonPath.read(
                replacementBooking.getResponse().getContentAsString(), "$.id").toString();
        assertThat(replacementId).isNotEqualTo(bookingId);
    }

    private String bookingJson(String propertyId, String roomTypeId,
                               LocalDate checkIn, LocalDate checkOut) {
        return """
                {
                  "propertyId": "%s",
                  "roomTypeId": "%s",
                  "checkIn": "%s",
                  "checkOut": "%s",
                  "guests": 2
                }
                """.formatted(propertyId, roomTypeId, checkIn, checkOut);
    }

    private String postCreated(String url, String json) throws Exception {
        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
