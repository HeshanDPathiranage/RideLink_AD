package com.example.fare_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MongoDB Document representing an isolated payment record for the RideLink Fare & Payment Service.
 * Follows the Database-Per-Service pattern with its own collection in MongoDB Atlas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Indexed
    private String rideId;

    @Indexed
    private String passengerId;

    private BigDecimal amount;

    private String currency;

    private String paymentMethod;

    private PaymentStatus status;

    @Indexed(unique = true)
    private String transactionId;

    private String failureReason;

    private LocalDateTime paymentDate;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
