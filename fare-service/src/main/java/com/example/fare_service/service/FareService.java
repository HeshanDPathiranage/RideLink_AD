package com.example.fare_service.service;

import com.example.fare_service.document.Payment;
import com.example.fare_service.document.PaymentStatus;
import com.example.fare_service.dto.FareRequestDto;
import com.example.fare_service.dto.FareResponseDto;
import com.example.fare_service.dto.FinalFareRequestDto;
import com.example.fare_service.dto.PaymentReceiptDto;
import com.example.fare_service.dto.PaymentRequestDto;
import com.example.fare_service.exception.PaymentProcessingException;
import com.example.fare_service.exception.ResourceNotFoundException;
import com.example.fare_service.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service layer implementing business logic for fare estimations, final calculations,
 * simulated payment processing, and receipt retrieval.
 * Follows SOLID principles and strict N-Tier architecture.
 */
@Slf4j
@Service
public class FareService {

    // Pricing Constants as defined by RideLink requirements
    public static final BigDecimal BASE_FARE = new BigDecimal("5.00");
    public static final BigDecimal RATE_PER_KM = new BigDecimal("2.00");
    public static final BigDecimal RATE_PER_WAITING_MINUTE = new BigDecimal("0.50");
    public static final String DEFAULT_CURRENCY = "USD";

    private final PaymentRepository paymentRepository;

    /**
     * Constructor Injection (avoiding @Autowired on fields to follow SOLID and clean architecture).
     */
    public FareService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * 1. Fare Estimation Workflow:
     * Calculates estimated fare based on distance (Rule: Base fare $5.00 + $2.00 per kilometer).
     *
     * @param requestDto contains the estimated distance in kilometers.
     * @return FareResponseDto containing estimated fare and calculation breakdown.
     */
    public FareResponseDto estimateFare(FareRequestDto requestDto) {
        log.info("Estimating fare for distance: {} km", requestDto.getDistanceKm());

        if (requestDto.getDistanceKm() == null || requestDto.getDistanceKm() <= 0) {
            throw new IllegalArgumentException("Distance must be greater than zero");
        }

        BigDecimal distance = BigDecimal.valueOf(requestDto.getDistanceKm());
        BigDecimal distanceFare = distance.multiply(RATE_PER_KM).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalEstimatedFare = BASE_FARE.add(distanceFare).setScale(2, RoundingMode.HALF_UP);

        String breakdown = String.format("Base Fare: $%s + Distance Fare (%s km * $%s/km): $%s = Total: $%s",
                BASE_FARE, distance, RATE_PER_KM, distanceFare, totalEstimatedFare);

        return FareResponseDto.builder()
                .distanceKm(requestDto.getDistanceKm())
                .baseFare(BASE_FARE)
                .distanceFare(distanceFare)
                .waitingCharge(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .tollFee(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .discountAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .surgeMultiplier(1.0)
                .totalFare(totalEstimatedFare)
                .currency(DEFAULT_CURRENCY)
                .calculationBreakdown(breakdown)
                .build();
    }

    /**
     * 2. Final Fare Calculation Workflow:
     * Computes the final trip fare incorporating actual distance, waiting times, tolls, discounts, and surge pricing.
     *
     * @param requestDto detailed final trip parameters.
     * @return FareResponseDto with finalized pricing breakdown.
     */
    public FareResponseDto calculateFinalFare(FinalFareRequestDto requestDto) {
        log.info("Calculating final fare for ride: {}, distance: {} km", requestDto.getRideId(), requestDto.getDistanceKm());

        BigDecimal distance = BigDecimal.valueOf(requestDto.getDistanceKm());
        BigDecimal distanceFare = distance.multiply(RATE_PER_KM).setScale(2, RoundingMode.HALF_UP);

        // Calculate waiting fee ($0.50 per minute)
        int waitMinutes = requestDto.getWaitingTimeMinutes() != null ? requestDto.getWaitingTimeMinutes() : 0;
        BigDecimal waitingCharge = BigDecimal.valueOf(waitMinutes).multiply(RATE_PER_WAITING_MINUTE).setScale(2, RoundingMode.HALF_UP);

        // Tolls and surcharges
        BigDecimal tollFee = requestDto.getTollFee() != null ? requestDto.getTollFee() : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = requestDto.getDiscountAmount() != null ? requestDto.getDiscountAmount() : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        double surgeMultiplier = (requestDto.getSurgeMultiplier() != null && requestDto.getSurgeMultiplier() >= 1.0)
                ? requestDto.getSurgeMultiplier() : 1.0;

        // Subtotal before tolls and discounts, subjected to surge
        BigDecimal basePlusDistancePlusWait = BASE_FARE.add(distanceFare).add(waitingCharge);
        BigDecimal surgedAmount = basePlusDistancePlusWait.multiply(BigDecimal.valueOf(surgeMultiplier)).setScale(2, RoundingMode.HALF_UP);

        // Final total calculation: (Surged Subtotal + Tolls) - Discount
        BigDecimal totalFare = surgedAmount.add(tollFee).subtract(discount).setScale(2, RoundingMode.HALF_UP);
        if (totalFare.compareTo(BigDecimal.ZERO) < 0) {
            totalFare = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        String breakdown = String.format("[(Base: $%s + Distance: $%s + Wait: $%s) * Surge (%sx)] + Tolls: $%s - Discount: $%s = Final Total: $%s",
                BASE_FARE, distanceFare, waitingCharge, surgeMultiplier, tollFee, discount, totalFare);

        return FareResponseDto.builder()
                .rideId(requestDto.getRideId())
                .distanceKm(requestDto.getDistanceKm())
                .baseFare(BASE_FARE)
                .distanceFare(distanceFare)
                .waitingCharge(waitingCharge)
                .tollFee(tollFee)
                .discountAmount(discount)
                .surgeMultiplier(surgeMultiplier)
                .totalFare(totalFare)
                .currency(DEFAULT_CURRENCY)
                .calculationBreakdown(breakdown)
                .build();
    }

    /**
     * 3. Simulated Payment Recording Workflow:
     * Records a simulated payment into MongoDB Atlas with statuses (PENDING, COMPLETED, FAILED).
     *
     * @param requestDto payment details.
     * @return PaymentReceiptDto representing generated payment transaction receipt.
     */
    public PaymentReceiptDto recordPayment(PaymentRequestDto requestDto) {
        log.info("Processing simulated payment for ride: {}, passenger: {}, amount: {}",
                requestDto.getRideId(), requestDto.getPassengerId(), requestDto.getAmount());

        // Check if a completed payment already exists for this ride
        if (paymentRepository.existsByRideIdAndStatus(requestDto.getRideId(), PaymentStatus.COMPLETED)) {
            log.warn("Ride {} already has a completed payment", requestDto.getRideId());
            throw new PaymentProcessingException("Ride " + requestDto.getRideId() + " already has a completed payment");
        }

        String transactionId = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        // Simulate payment logic: allow explicit failure simulation or decline for invalid amounts
        PaymentStatus paymentStatus;
        String failureReason = null;

        if (Boolean.TRUE.equals(requestDto.getSimulateFailure()) || requestDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            paymentStatus = PaymentStatus.FAILED;
            failureReason = "Payment transaction declined by payment gateway or insufficient funds.";
            log.warn("Payment simulation failed for ride: {}. Reason: {}", requestDto.getRideId(), failureReason);
        } else {
            paymentStatus = PaymentStatus.COMPLETED;
            log.info("Payment simulation succeeded for ride: {} with transaction ID: {}", requestDto.getRideId(), transactionId);
        }

        String currency = requestDto.getCurrency() != null && !requestDto.getCurrency().isBlank()
                ? requestDto.getCurrency() : DEFAULT_CURRENCY;

        Payment payment = Payment.builder()
                .rideId(requestDto.getRideId())
                .passengerId(requestDto.getPassengerId())
                .amount(requestDto.getAmount().setScale(2, RoundingMode.HALF_UP))
                .currency(currency)
                .paymentMethod(requestDto.getPaymentMethod().toUpperCase())
                .status(paymentStatus)
                .transactionId(transactionId)
                .failureReason(failureReason)
                .paymentDate(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return mapToReceiptDto(savedPayment);
    }

    /**
     * 4. Receipt Retrieval Workflow:
     * Retrieves an official payment receipt by payment ID.
     *
     * @param paymentId MongoDB payment document ID.
     * @return PaymentReceiptDto DTO representation of the receipt.
     */
    public PaymentReceiptDto getReceiptByPaymentId(String paymentId) {
        log.info("Fetching receipt for paymentId: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with ID: " + paymentId));

        return mapToReceiptDto(payment);
    }

    /**
     * 4. Receipt Retrieval Workflow (by Ride ID):
     * Retrieves an official payment receipt by associated ride ID.
     *
     * @param rideId associated ride identifier.
     * @return PaymentReceiptDto DTO representation of the receipt.
     */
    public PaymentReceiptDto getReceiptByRideId(String rideId) {
        log.info("Fetching receipt for rideId: {}", rideId);

        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for ride ID: " + rideId));

        return mapToReceiptDto(payment);
    }

    /**
     * Helper mapper method: strictly transforms internal Payment entity into external PaymentReceiptDto
     * ensuring database document entities are never directly exposed to the client.
     */
    private PaymentReceiptDto mapToReceiptDto(Payment payment) {
        String receiptNumber = "RCP-" + payment.getTransactionId();
        String notes = payment.getStatus() == PaymentStatus.COMPLETED
                ? "Payment successfully captured. Thank you for riding with RideLink!"
                : "Payment transaction unfulfilled. Please update your payment method.";

        return PaymentReceiptDto.builder()
                .receiptNumber(receiptNumber)
                .paymentId(payment.getId())
                .transactionId(payment.getTransactionId())
                .rideId(payment.getRideId())
                .passengerId(payment.getPassengerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .failureReason(payment.getFailureReason())
                .issuedAt(LocalDateTime.now())
                .notes(notes)
                .build();
    }
}
