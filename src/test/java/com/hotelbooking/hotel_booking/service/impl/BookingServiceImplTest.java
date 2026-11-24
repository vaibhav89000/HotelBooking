package com.hotelbooking.hotel_booking.service.impl;

import com.hotelbooking.hotel_booking.dto.request.CreateBookingRequest;
import com.hotelbooking.hotel_booking.dto.request.CreateOwnerRequest;
import com.hotelbooking.hotel_booking.dto.request.OnboardPropertyRequest;
import com.hotelbooking.hotel_booking.dto.request.OnboardPropertyRequest.RoomTypeRequest;
import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.exception.DomainValidationException;
import com.hotelbooking.hotel_booking.exception.ResourceNotFoundException;
import com.hotelbooking.hotel_booking.exception.RoomUnavailableException;
import com.hotelbooking.hotel_booking.lock.InMemoryLockManager;
import com.hotelbooking.hotel_booking.model.Booking;
import com.hotelbooking.hotel_booking.model.Property;
import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryBookingRepository;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryOwnerRepository;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryPropertyRepository;
import com.hotelbooking.hotel_booking.repository.inmemory.InMemoryRoomInventoryRepository;
import com.hotelbooking.hotel_booking.service.OwnerService;
import com.hotelbooking.hotel_booking.service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingServiceImplTest {

    private static final LocalDate DEC_10 = LocalDate.of(2026, 12, 10);

    private final InMemoryRoomInventoryRepository inventoryRepository = new InMemoryRoomInventoryRepository();
    private final InMemoryBookingRepository bookingRepository = new InMemoryBookingRepository();
    private final OwnerService ownerService = new OwnerServiceImpl(new InMemoryOwnerRepository());
    private final PropertyService propertyService =
            new PropertyServiceImpl(ownerService, new InMemoryPropertyRepository(), inventoryRepository);
    private final BookingServiceImpl bookingService =
            new BookingServiceImpl(propertyService, bookingRepository, inventoryRepository, new InMemoryLockManager());

    private Property property;
    private RoomType deluxe;

    @BeforeEach
    void setUp() {
        property = onboardPropertyWithRooms(1);
        deluxe = property.getRoomTypes().iterator().next();
    }

    @Test
    void createsPendingBookingPricedPerNight() {
        Booking booking = bookingService.createBooking(request(DEC_10, DEC_10.plusDays(3), 2));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(booking.getTotalAmount()).isEqualByComparingTo("10500");
        assertThat(bookingService.getBooking(booking.getId())).isSameAs(booking);
    }

    @Test
    void reservesTheBookedNights() {
        bookingService.createBooking(request(DEC_10, DEC_10.plusDays(2), 2));

        var inventory = inventoryRepository.findById(deluxe.getId()).orElseThrow();
        assertThat(inventory.availableOn(DEC_10)).isZero();
        assertThat(inventory.availableOn(DEC_10.plusDays(1))).isZero();
        assertThat(inventory.availableOn(DEC_10.plusDays(2))).isEqualTo(1);
    }

    @Test
    void rejectsOverlappingBookingOnLastRoom() {
        bookingService.createBooking(request(DEC_10, DEC_10.plusDays(3), 2));

        assertThatThrownBy(() -> bookingService.createBooking(request(DEC_10.plusDays(2), DEC_10.plusDays(4), 2)))
                .isInstanceOf(RoomUnavailableException.class);
    }

    @Test
    void allowsBackToBackBookings() {
        bookingService.createBooking(request(DEC_10, DEC_10.plusDays(2), 2));

        Booking next = bookingService.createBooking(request(DEC_10.plusDays(2), DEC_10.plusDays(4), 2));

        assertThat(next.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void rejectsMoreGuestsThanRoomAllows() {
        assertThatThrownBy(() -> bookingService.createBooking(request(DEC_10, DEC_10.plusDays(1), 3)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("at most 2 guests");
    }

    @Test
    void rejectsCheckOutBeforeCheckIn() {
        assertThatThrownBy(() -> bookingService.createBooking(request(DEC_10, DEC_10.minusDays(1), 2)))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsUnknownRoomType() {
        CreateBookingRequest unknownRoom =
                new CreateBookingRequest(property.getId(), "missing", DEC_10, DEC_10.plusDays(1), 2);

        assertThatThrownBy(() -> bookingService.createBooking(unknownRoom))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void onlyOneOfManyConcurrentBookingsGetsTheLastRoom() throws Exception {
        int successes = bookConcurrently(20);

        assertThat(successes).isEqualTo(1);
        assertThat(bookingRepository.findAll()).hasSize(1);
    }

    @Test
    void concurrentBookingsNeverExceedTotalRooms() throws Exception {
        property = onboardPropertyWithRooms(5);
        deluxe = property.getRoomTypes().iterator().next();

        int successes = bookConcurrently(20);

        assertThat(successes).isEqualTo(5);
        assertThat(inventoryRepository.findById(deluxe.getId()).orElseThrow().availableOn(DEC_10)).isZero();
    }

    private int bookConcurrently(int attempts) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < attempts; i++) {
            results.add(executor.submit(() -> {
                start.await();
                try {
                    bookingService.createBooking(request(DEC_10, DEC_10.plusDays(2), 2));
                    return true;
                } catch (RoomUnavailableException e) {
                    return false;
                }
            }));
        }
        start.countDown();

        int successes = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                successes++;
            }
        }
        executor.shutdown();
        return successes;
    }

    private Property onboardPropertyWithRooms(int totalRooms) {
        String ownerId = ownerService.createOwner(new CreateOwnerRequest("Lakeview Stays")).getId();
        OnboardPropertyRequest request = new OnboardPropertyRequest("Lakeview", "12 MG Road", "Indiranagar",
                "Bengaluru", 4, Set.of(),
                List.of(new RoomTypeRequest("Deluxe", 2, totalRooms, new BigDecimal("3500"))));
        return propertyService.onboardProperty(ownerId, request);
    }

    private CreateBookingRequest request(LocalDate checkIn, LocalDate checkOut, int guests) {
        return new CreateBookingRequest(property.getId(), deluxe.getId(), checkIn, checkOut, guests);
    }
}
