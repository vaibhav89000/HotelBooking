package com.hotelbooking.hotel_booking.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    private static final LocalDate CHECK_IN = LocalDate.now().plusDays(10);

    @Autowired
    private MockMvc mockMvc;

    private String propertyId;
    private String roomTypeId;

    @BeforeEach
    void onboardProperty() throws Exception {
        String owner = postCreated("/api/owners", """
                { "name": "Grand Hotels Group" }
                """);
        String property = postCreated("/api/owners/" + JsonPath.read(owner, "$.id") + "/properties", """
                {
                  "name": "Lakeview", "addressLine": "12 MG Road", "locality": "Indiranagar", "city": "Bengaluru",
                  "starRating": 4,
                  "roomTypes": [ { "name": "Deluxe", "maxOccupancy": 2, "totalRooms": 1, "pricePerNight": 3500 } ]
                }
                """);
        propertyId = JsonPath.read(property, "$.id");
        roomTypeId = JsonPath.read(property, "$.roomTypes[0].id");
    }

    @Test
    void createsAndFetchesBooking() throws Exception {
        String booking = postCreated("/api/bookings", bookingJson(CHECK_IN, CHECK_IN.plusDays(2), 2));

        mockMvc.perform(get("/api/bookings/{id}", JsonPath.read(booking, "$.id").toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalAmount").value(7000))
                .andExpect(jsonPath("$.checkIn").value(CHECK_IN.toString()));
    }

    @Test
    void returnsConflictWhenRoomIsTaken() throws Exception {
        postCreated("/api/bookings", bookingJson(CHECK_IN, CHECK_IN.plusDays(2), 2));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(CHECK_IN.plusDays(1), CHECK_IN.plusDays(3), 2)))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsCheckInInThePast() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(LocalDate.now().minusDays(1), CHECK_IN, 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.checkIn").exists());
    }

    private String bookingJson(LocalDate checkIn, LocalDate checkOut, int guests) {
        return """
                { "propertyId": "%s", "roomTypeId": "%s", "checkIn": "%s", "checkOut": "%s", "guests": %d }
                """.formatted(propertyId, roomTypeId, checkIn, checkOut, guests);
    }

    private String postCreated(String url, String json) throws Exception {
        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}
