package com.example.fare_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for fare estimation requests based on distance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareRequestDto {

    @NotNull(message = "Distance in kilometers is required")
    @Positive(message = "Distance in kilometers must be greater than zero")
    private Double distanceKm;

    private String vehicleType; // e.g. "STANDARD", "PREMIUM"
}
