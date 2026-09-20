package com.example.fare_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for calculating the final trip fare upon ride completion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalFareRequestDto {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @NotNull(message = "Final distance in kilometers is required")
    @Positive(message = "Distance in kilometers must be greater than zero")
    private Double distanceKm;

    @Min(value = 0, message = "Waiting time cannot be negative")
    private Integer waitingTimeMinutes;

    @DecimalMin(value = "0.0", inclusive = true, message = "Toll fee cannot be negative")
    private BigDecimal tollFee;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount cannot be negative")
    private BigDecimal discountAmount;

    @DecimalMin(value = "1.0", inclusive = true, message = "Surge multiplier must be at least 1.0")
    private Double surgeMultiplier;
}
