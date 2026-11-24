package com.hotelbooking.hotel_booking.service.impl;

import com.hotelbooking.hotel_booking.dto.request.CreateBookingRequest;
import com.hotelbooking.hotel_booking.dto.request.CreateOwnerRequest;
import com.hotelbooking.hotel_booking.dto.request.OnboardPropertyRequest;
import com.hotelbooking.hotel_booking.dto.request.OnboardPropertyRequest.RoomTypeRequest;
import com.hotelbooking.hotel_booking.dto.request.SearchRequest;
import com.hotelbooking.hotel_booking.enums.Amenity;
import com.hotelbooking.hotel_booking.lock.InMemoryLockManager;
import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.model.SearchResult;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryBookingRepository;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryOwnerRepository;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryPropertyRepository;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryRoomInventoryRepository;
import com.hotelbooking.hotel_booking.service.BookingService;
import com.hotelbooking.hotel_booking.service.OwnerService;
import com.hotelbooking.hotel_booking.service.PropertyService;
import com.hotelbooking.hotel_booking.service.filter.AmenitiesFilter;
import com.hotelbooking.hotel_booking.service.filter.AvailabilityFilter;
import com.hotelbooking.hotel_booking.service.filter.GuestCapacityFilter;
import com.hotelbooking.hotel_booking.service.filter.LocalityFilter;
import com.hotelbooking.hotel_booking.service.filter.PriceRangeFilter;
import com.hotelbooking.hotel_booking.service.filter.StarRatingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SearchServiceImplTest {

    private static final LocalDate DEC_10 = LocalDate.of(2026, 12, 10);

    private final InMemoryPropertyRepository propertyRepository = new InMemoryPropertyRepository();
    private final InMemoryRoomInventoryRepository inventoryRepository = new InMemoryRoomInventoryRepository();
    private final OwnerService ownerService = new OwnerServiceImpl(new InMemoryOwnerRepository());
    private final PropertyService propertyService =
            new PropertyServiceImpl(ownerService, propertyRepository, inventoryRepository);
    private final BookingService bookingService = new BookingServiceImpl(
            propertyService, new InMemoryBookingRepository(), inventoryRepository, new InMemoryLockManager());
    private final SearchServiceImpl searchService = new SearchServiceImpl(propertyRepository, List.of(
            new GuestCapacityFilter(),
            new LocalityFilter(),
            new PriceRangeFilter(),
            new AmenitiesFilter(),
            new StarRatingFilter(),
            new AvailabilityFilter(inventoryRepository)));

    private Property lakeview;
    private Property budgetInn;

    @BeforeEach
    void setUp() {
        String ownerId = ownerService.createOwner(new CreateOwnerRequest("Grand Hotels Group")).getId();
        lakeview = propertyService.onboardProperty(ownerId, new OnboardPropertyRequest(
                "Lakeview", "12 MG Road", "Indiranagar", "Bengaluru", 4, Set.of(Amenity.WIFI, Amenity.POOL),
                List.of(room("Deluxe", 2, "3500"), room("Suite", 4, "8000"))));
        budgetInn = propertyService.onboardProperty(ownerId, new OnboardPropertyRequest(
                "Budget Inn", "5 Hosur Road", "Koramangala", "Bengaluru", 2, Set.of(Amenity.WIFI),
                List.of(room("Standard", 2, "1500"))));
        propertyService.onboardProperty(ownerId, new OnboardPropertyRequest(
                "Sea Breeze", "1 Marine Drive", "Colaba", "Mumbai", 5, Set.of(Amenity.WIFI, Amenity.POOL),
                List.of(room("Deluxe", 2, "6000"))));
    }

    @Test
    void returnsOnlyPropertiesInTheRequestedCity() {
        assertThat(propertyNames(searchService.search(search()))).containsExactlyInAnyOrder("Lakeview", "Budget Inn");
    }

    @Test
    void returnsOnlyRoomTypesThatFitTheGuests() {
        SearchRequest request = search();
        request.setGuests(3);

        List<SearchResult> results = searchService.search(request);

        assertThat(propertyNames(results)).containsExactly("Lakeview");
        assertThat(roomNames(results.get(0))).containsExactly("Suite");
    }

    @Test
    void hidesRoomTypesBookedForTheRequestedDates() {
        book(lakeview, "Deluxe", DEC_10, DEC_10.plusDays(2));

        SearchResult result = resultFor("Lakeview", searchService.search(search()));

        assertThat(roomNames(result)).containsExactly("Suite");
    }

    @Test
    void hidesPropertyWhenAllRoomTypesAreBooked() {
        book(lakeview, "Deluxe", DEC_10, DEC_10.plusDays(2));
        book(lakeview, "Suite", DEC_10.plusDays(1), DEC_10.plusDays(3));

        assertThat(propertyNames(searchService.search(search()))).containsExactly("Budget Inn");
    }

    @Test
    void showsRoomAgainForDatesOutsideTheBooking() {
        book(lakeview, "Deluxe", DEC_10, DEC_10.plusDays(2));
        SearchRequest request = search();
        request.setCheckIn(DEC_10.plusDays(2));
        request.setCheckOut(DEC_10.plusDays(4));

        assertThat(roomNames(resultFor("Lakeview", searchService.search(request)))).contains("Deluxe");
    }

    @Test
    void filtersByPriceRange() {
        SearchRequest request = search();
        request.setMaxPrice(new BigDecimal("2000"));

        assertThat(propertyNames(searchService.search(request))).containsExactly("Budget Inn");
    }

    @Test
    void filtersByAmenities() {
        SearchRequest request = search();
        request.setAmenities(Set.of(Amenity.POOL));

        assertThat(propertyNames(searchService.search(request))).containsExactly("Lakeview");
    }

    @Test
    void filtersByMinimumStarRating() {
        SearchRequest request = search();
        request.setMinStars(4);

        assertThat(propertyNames(searchService.search(request))).containsExactly("Lakeview");
    }

    @Test
    void filtersByLocality() {
        SearchRequest request = search();
        request.setLocality("koramangala");

        assertThat(propertyNames(searchService.search(request))).containsExactly("Budget Inn");
    }

    @Test
    void combinesFilters() {
        SearchRequest request = search();
        request.setAmenities(Set.of(Amenity.WIFI));
        request.setMinStars(3);
        request.setMaxPrice(new BigDecimal("4000"));

        List<SearchResult> results = searchService.search(request);

        assertThat(propertyNames(results)).containsExactly("Lakeview");
        assertThat(roomNames(results.get(0))).containsExactly("Deluxe");
    }

    private SearchRequest search() {
        SearchRequest request = new SearchRequest();
        request.setCity("Bengaluru");
        request.setCheckIn(DEC_10);
        request.setCheckOut(DEC_10.plusDays(2));
        request.setGuests(2);
        return request;
    }

    private void book(Property property, String roomName, LocalDate checkIn, LocalDate checkOut) {
        RoomType roomType = property.getRoomTypes().stream()
                .filter(room -> room.getName().equals(roomName))
                .findFirst().orElseThrow();
        bookingService.createBooking(new CreateBookingRequest(property.getId(), roomType.getId(), checkIn, checkOut, 2));
    }

    private static RoomTypeRequest room(String name, int maxOccupancy, String price) {
        return new RoomTypeRequest(name, maxOccupancy, 1, new BigDecimal(price));
    }

    private static List<String> propertyNames(List<SearchResult> results) {
        return results.stream().map(result -> result.getProperty().getName()).toList();
    }

    private static List<String> roomNames(SearchResult result) {
        return result.getRoomTypes().stream().map(RoomType::getName).toList();
    }

    private static SearchResult resultFor(String propertyName, List<SearchResult> results) {
        return results.stream()
                .filter(result -> result.getProperty().getName().equals(propertyName))
                .findFirst().orElseThrow();
    }
}
