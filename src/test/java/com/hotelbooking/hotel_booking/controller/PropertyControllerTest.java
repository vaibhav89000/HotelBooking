package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.model.Owner;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PropertyControllerTest {

    private static final String LAKEVIEW = """
            {
              "name": "Lakeview",
              "addressLine": "12 MG Road",
              "locality": "Indiranagar",
              "city": "Bengaluru",
              "starRating": 4,
              "amenities": ["WIFI", "POOL"],
              "roomTypes": [
                { "name": "Deluxe", "maxOccupancy": 2, "totalRooms": 10, "pricePerNight": 3500 }
              ]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void onboardsPropertyForOwner() throws Exception {
        String ownerId = createOwner();

        String body = mockMvc.perform(post("/api/owners/{ownerId}/properties", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LAKEVIEW))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andExpect(jsonPath("$.location.city").value("Bengaluru"))
                .andExpect(jsonPath("$.roomTypes[0].name").value("Deluxe"))
                .andExpect(jsonPath("$.roomTypes[0].id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String propertyId = JsonPath.read(body, "$.id");
        mockMvc.perform(get("/api/properties/{propertyId}", propertyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lakeview"));
    }

    @Test
    void rejectsInvalidRequestWithFieldErrors() throws Exception {
        String ownerId = createOwner();

        mockMvc.perform(post("/api/owners/{ownerId}/properties", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "", "starRating": 9, "roomTypes": [] }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.city").exists())
                .andExpect(jsonPath("$.errors.starRating").exists())
                .andExpect(jsonPath("$.errors.roomTypes").exists());
    }

    @Test
    void returnsNotFoundForUnknownOwner() throws Exception {
        mockMvc.perform(post("/api/owners/{ownerId}/properties", "missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LAKEVIEW))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Owner not found: missing"));
    }

    @Test
    void searchReturnsAvailablePropertiesWithStayPrice() throws Exception {
        String city = "City-" + UUID.randomUUID();
        mockMvc.perform(post("/api/owners/{ownerId}/properties", createOwner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LAKEVIEW.replace("Bengaluru", city)))
                .andExpect(status().isCreated());
        LocalDate checkIn = LocalDate.now().plusDays(5);

        mockMvc.perform(get("/api/properties/search")
                        .param("city", city)
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkIn.plusDays(2).toString())
                        .param("guests", "2")
                        .param("amenities", "WIFI,POOL")
                        .param("minStars", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Lakeview"))
                .andExpect(jsonPath("$[0].availableRooms[0].name").value("Deluxe"))
                .andExpect(jsonPath("$[0].availableRooms[0].totalPrice").value(7000));
    }

    @Test
    void searchRejectsMissingCityAndInvalidDates() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(5);

        mockMvc.perform(get("/api/properties/search")
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkIn.minusDays(1).toString())
                        .param("guests", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.city").exists())
                .andExpect(jsonPath("$.errors.checkOutAfterCheckIn").exists());
    }

    private String createOwner() throws Exception {
        String body = mockMvc.perform(post("/api/owners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Grand Hotels Group" }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }
}
