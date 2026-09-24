package com.example.fare_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object returning fare estimation and calculation breakdowns.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareResponseDto {

    private String rideId;
    private Double distanceKm;
    private BigDecimal baseFare;
    private BigDecimal distanceFare;
    private BigDecimal waitingCharge;
    private BigDecimal tollFee;
    private BigDecimal discountAmount;
    private Double surgeMultiplier;
    private BigDecimal totalFare;
    private String currency;
    private String calculationBreakdown;
}
