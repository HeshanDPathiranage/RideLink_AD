package com.example.fare_service.dto;

import com.example.fare_service.document.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing an official payment receipt for a ride.
 * Used for receipt generation and retrieval to prevent exposing internal entities directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReceiptDto {

    private String receiptNumber;
    private String paymentId;
    private String transactionId;
    private String rideId;
    private String passengerId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private String failureReason;
    private LocalDateTime issuedAt;
    private String notes;
}
