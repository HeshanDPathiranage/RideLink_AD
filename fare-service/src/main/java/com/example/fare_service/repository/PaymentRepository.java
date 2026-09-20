package com.example.fare_service.repository;

import com.example.fare_service.document.Payment;
import com.example.fare_service.document.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Layer for MongoDB Atlas `payments` collection.
 */
@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByRideId(String rideId);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByPassengerId(String passengerId);

    List<Payment> findByStatus(PaymentStatus status);

    boolean existsByRideIdAndStatus(String rideId, PaymentStatus status);
}
