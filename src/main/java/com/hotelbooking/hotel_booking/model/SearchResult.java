package com.hotelbooking.hotel_booking.model;

import lombok.Value;

import java.util.List;

@Value
public class SearchResult {

    Property property;
    List<RoomType> roomTypes;
}
