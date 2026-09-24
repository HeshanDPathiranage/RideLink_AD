package com.example.fare_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for initiating a simulated payment recording.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @NotBlank(message = "Passenger ID is required")
    private String passengerId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    private BigDecimal amount;

    private String currency; // Defaults to USD if not specified

    @NotBlank(message = "Payment method is required (e.g., CREDIT_CARD, WALLET, CASH)")
    private String paymentMethod;

    /**
     * Optional testing flag to simulate failure scenarios (e.g. insufficient funds)
     */
    private Boolean simulateFailure;
}
