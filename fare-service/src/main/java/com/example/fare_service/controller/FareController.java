package com.example.fare_service.controller;

import com.example.fare_service.dto.FareRequestDto;
import com.example.fare_service.dto.FareResponseDto;
import com.example.fare_service.dto.FinalFareRequestDto;
import com.example.fare_service.dto.PaymentReceiptDto;
import com.example.fare_service.dto.PaymentRequestDto;
import com.example.fare_service.service.FareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing Fare & Payment Service endpoints for the RideLink platform.
 * Follows N-Tier Architecture, constructor injection, and strictly deals with DTOs.
 */
@RestController
@RequestMapping("/api/v1/fares")
public class FareController {

    private final FareService fareService;

    /**
     * Constructor-based dependency injection (adheres to SOLID Dependency Inversion principle).
     */
    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    /**
     * 1. Fare Estimation Endpoint:
     * Estimates fare based on distance (Rule: Base fare $5.00 + $2.00 per km).
     *
     * @param requestDto validated estimation parameters.
     * @return 200 OK with FareResponseDto.
     */
    @PostMapping("/estimate")
    public ResponseEntity<FareResponseDto> estimateFare(@Valid @RequestBody FareRequestDto requestDto) {
        FareResponseDto response = fareService.estimateFare(requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. Final Fare Calculation Endpoint:
     * Calculates the definitive fare upon ride completion considering distance, waiting time, tolls, and surge.
     *
     * @param requestDto validated final trip metrics.
     * @return 200 OK with FareResponseDto.
     */
    @PostMapping("/calculate-final")
    public ResponseEntity<FareResponseDto> calculateFinalFare(@Valid @RequestBody FinalFareRequestDto requestDto) {
        FareResponseDto response = fareService.calculateFinalFare(requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. Simulated Payment Recording Endpoint:
     * Records a simulated payment into the isolated MongoDB database and returns a receipt.
     *
     * @param requestDto validated payment request data.
     * @return 201 CREATED with PaymentReceiptDto.
     */
    @PostMapping("/payments")
    public ResponseEntity<PaymentReceiptDto> recordPayment(@Valid @RequestBody PaymentRequestDto requestDto) {
        PaymentReceiptDto receipt = fareService.recordPayment(requestDto);
        return new ResponseEntity<>(receipt, HttpStatus.CREATED);
    }

    /**
     * 4. Receipt Retrieval Endpoint (by Payment ID):
     * Fetches a generated receipt using the unique payment record ID.
     *
     * @param paymentId payment document ID in MongoDB.
     * @return 200 OK with PaymentReceiptDto.
     */
    @GetMapping("/payments/{paymentId}/receipt")
    public ResponseEntity<PaymentReceiptDto> getReceiptByPaymentId(@PathVariable String paymentId) {
        PaymentReceiptDto receipt = fareService.getReceiptByPaymentId(paymentId);
        return ResponseEntity.ok(receipt);
    }

    /**
     * 4. Receipt Retrieval Endpoint (by Ride ID):
     * Fetches a generated receipt using the associated ride ID.
     *
     * @param rideId ride identifier.
     * @return 200 OK with PaymentReceiptDto.
     */
    @GetMapping("/payments/ride/{rideId}/receipt")
    public ResponseEntity<PaymentReceiptDto> getReceiptByRideId(@PathVariable String rideId) {
        PaymentReceiptDto receipt = fareService.getReceiptByRideId(rideId);
        return ResponseEntity.ok(receipt);
    }
}
