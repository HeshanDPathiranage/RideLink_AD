package com.example.ride_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "rides")
public class Ride {

    @Id
    private String id;

    private Long passengerId;
    private Long driverId;
    private String pickupLocation;
    private String destination;
    private RideStatus status;
    private Double estimatedFare;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum RideStatus {
        REQUESTED,
        ASSIGNED,
        ACCEPTED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    public Ride() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = RideStatus.REQUESTED;
    }

    public Ride(Long passengerId, String pickupLocation, String destination, Double estimatedFare) {
        this();
        this.passengerId = passengerId;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.estimatedFare = estimatedFare;
    }

    public Ride(String id, Long passengerId, Long driverId, String pickupLocation, String destination,
                RideStatus status, Double estimatedFare, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.status = status;
        this.estimatedFare = estimatedFare;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public Double getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(Double estimatedFare) {
        this.estimatedFare = estimatedFare;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}