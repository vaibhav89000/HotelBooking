package com.hotelbooking.hotel_booking.dto.request;

import com.hotelbooking.hotel_booking.enums.Amenity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardPropertyRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String addressLine;

    @NotBlank
    private String locality;

    @NotBlank
    private String city;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer starRating;

    private Set<@NotNull Amenity> amenities;

    @NotEmpty
    private List<@NotNull @Valid RoomTypeRequest> roomTypes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomTypeRequest {

        @NotBlank
        private String name;

        @NotNull
        @Positive
        private Integer maxOccupancy;

        @NotNull
        @Positive
        private Integer totalRooms;

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal pricePerNight;
    }
}
