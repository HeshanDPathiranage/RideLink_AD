package com.example.fare_service.service;

import com.example.fare_service.document.Payment;
import com.example.fare_service.document.PaymentStatus;
import com.example.fare_service.dto.FareRequestDto;
import com.example.fare_service.dto.FareResponseDto;
import com.example.fare_service.dto.FinalFareRequestDto;
import com.example.fare_service.dto.PaymentReceiptDto;
import com.example.fare_service.dto.PaymentRequestDto;
import com.example.fare_service.exception.ResourceNotFoundException;
import com.example.fare_service.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FareServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private FareService fareService;

    private Payment mockPayment;

    @BeforeEach
    void setUp() {
        mockPayment = Payment.builder()
                .id("pay123")
                .rideId("ride001")
                .passengerId("user123")
                .amount(new BigDecimal("25.00"))
                .currency("USD")
                .paymentMethod("CREDIT_CARD")
                .status(PaymentStatus.COMPLETED)
                .transactionId("TXN-1234567890AB")
                .paymentDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Fare estimation should compute Base Fare ($5.00) + $2.00 per km")
    void testEstimateFare() {
        // Given 10 kilometers: Expected = $5.00 + (10 * $2.00) = $25.00
        FareRequestDto request = FareRequestDto.builder()
                .distanceKm(10.0)
                .vehicleType("STANDARD")
                .build();

        FareResponseDto response = fareService.estimateFare(request);

        assertNotNull(response);
        assertEquals(10.0, response.getDistanceKm());
        assertEquals(new BigDecimal("5.00"), response.getBaseFare());
        assertEquals(new BigDecimal("20.00"), response.getDistanceFare());
        assertEquals(new BigDecimal("25.00"), response.getTotalFare());
    }

    @Test
    @DisplayName("Final fare calculation should accurately include wait time, surge, and tolls")
    void testCalculateFinalFare() {
        // Distance: 5 km -> Base $5 + Distance $10 + Wait (4 min * $0.50 = $2.00) = $17.00
        // Surge 1.5x -> $25.50 + Toll $4.50 - Discount $2.00 = $28.00
        FinalFareRequestDto request = FinalFareRequestDto.builder()
                .rideId("ride001")
                .distanceKm(5.0)
                .waitingTimeMinutes(4)
                .surgeMultiplier(1.5)
                .tollFee(new BigDecimal("4.50"))
                .discountAmount(new BigDecimal("2.00"))
                .build();

        FareResponseDto response = fareService.calculateFinalFare(request);

        assertNotNull(response);
        assertEquals("ride001", response.getRideId());
        assertEquals(new BigDecimal("28.00"), response.getTotalFare());
    }

    @Test
    @DisplayName("Simulated payment recording should persist to repository with COMPLETED status")
    void testRecordPaymentSuccess() {
        PaymentRequestDto request = PaymentRequestDto.builder()
                .rideId("ride001")
                .passengerId("user123")
                .amount(new BigDecimal("25.00"))
                .paymentMethod("CREDIT_CARD")
                .simulateFailure(false)
                .build();

        when(paymentRepository.existsByRideIdAndStatus("ride001", PaymentStatus.COMPLETED)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);

        PaymentReceiptDto receipt = fareService.recordPayment(request);

        assertNotNull(receipt);
        assertEquals(PaymentStatus.COMPLETED, receipt.getStatus());
        assertEquals("ride001", receipt.getRideId());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Simulated payment recording should mark status as FAILED when simulateFailure is true")
    void testRecordPaymentFailureSimulation() {
        Payment failedPayment = Payment.builder()
                .id("payFailed")
                .rideId("ride002")
                .passengerId("user456")
                .amount(new BigDecimal("15.00"))
                .status(PaymentStatus.FAILED)
                .failureReason("Payment transaction declined by payment gateway or insufficient funds.")
                .transactionId("TXN-FAILED999")
                .build();

        PaymentRequestDto request = PaymentRequestDto.builder()
                .rideId("ride002")
                .passengerId("user456")
                .amount(new BigDecimal("15.00"))
                .paymentMethod("CREDIT_CARD")
                .simulateFailure(true)
                .build();

        when(paymentRepository.existsByRideIdAndStatus("ride002", PaymentStatus.COMPLETED)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(failedPayment);

        PaymentReceiptDto receipt = fareService.recordPayment(request);

        assertNotNull(receipt);
        assertEquals(PaymentStatus.FAILED, receipt.getStatus());
        assertNotNull(receipt.getFailureReason());
    }

    @Test
    @DisplayName("Receipt retrieval by payment ID should throw ResourceNotFoundException if record missing")
    void testGetReceiptByPaymentIdNotFound() {
        when(paymentRepository.findById("unknownId")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fareService.getReceiptByPaymentId("unknownId"));
    }
}
