package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.request.SearchRequest;
import com.hotelbooking.hotel_booking.model.SearchResult;

import java.util.List;

public interface SearchService {

    List<SearchResult> search(SearchRequest request);
}
