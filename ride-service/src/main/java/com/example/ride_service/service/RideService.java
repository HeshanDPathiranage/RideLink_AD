package com.example.ride_service.service;

import com.example.ride_service.exception.ResourceNotFoundException;
import com.example.ride_service.model.Ride;
import com.example.ride_service.model.Ride.RideStatus;
import com.example.ride_service.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RideService {

    private final RideRepository rideRepository;

    @Autowired
    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    public Ride createRide(Ride ride) {
        if (ride == null) {
            throw new IllegalArgumentException("Ride details cannot be null.");
        }
        if (ride.getPassengerId() == null) {
            throw new IllegalArgumentException("Passenger ID is required.");
        }
        if (ride.getPickupLocation() == null || ride.getPickupLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Pickup location is required.");
        }
        if (ride.getDestination() == null || ride.getDestination().trim().isEmpty()) {
            throw new IllegalArgumentException("Destination is required.");
        }

        // Ensure newly created ride gets a new document ID and default status
        ride.setId(null);
        ride.setStatus(RideStatus.REQUESTED);
        ride.setCreatedAt(LocalDateTime.now());
        ride.setUpdatedAt(LocalDateTime.now());

        return rideRepository.save(ride);
    }

    public Ride assignDriver(String rideId, Long driverId) {
        if (driverId == null) {
            throw new IllegalArgumentException("Driver ID is required.");
        }

        Ride ride = getRideById(rideId);

        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new IllegalStateException("Ride cannot be assigned. Current status is: " + ride.getStatus());
        }

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setUpdatedAt(LocalDateTime.now());

        return rideRepository.save(ride);
    }

    public Ride updateRideStatus(String rideId, RideStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New ride status cannot be null.");
        }

        Ride ride = getRideById(rideId);
        RideStatus currentStatus = ride.getStatus();

        if (currentStatus == RideStatus.COMPLETED || currentStatus == RideStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update status of a ride that is already " + currentStatus);
        }

        boolean isValidTransition = switch (newStatus) {
            case ACCEPTED -> currentStatus == RideStatus.ASSIGNED;
            case IN_PROGRESS -> currentStatus == RideStatus.ACCEPTED;
            case COMPLETED -> currentStatus == RideStatus.IN_PROGRESS;
            case CANCELLED -> currentStatus == RideStatus.REQUESTED 
                           || currentStatus == RideStatus.ASSIGNED 
                           || currentStatus == RideStatus.ACCEPTED;
            default -> false;
        };

        if (!isValidTransition) {
            throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        ride.setStatus(newStatus);
        ride.setUpdatedAt(LocalDateTime.now());

        return rideRepository.save(ride);
    }

    public Ride getRideById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Ride ID cannot be blank.");
        }
        return rideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + id));
    }

    public List<Ride> getRidesByPassenger(Long passengerId) {
        if (passengerId == null) {
            throw new IllegalArgumentException("Passenger ID cannot be null.");
        }
        return rideRepository.findByPassengerId(passengerId);
    }

    public List<Ride> getRidesByDriver(Long driverId) {
        if (driverId == null) {
            throw new IllegalArgumentException("Driver ID cannot be null.");
        }
        return rideRepository.findByDriverId(driverId);
    }

    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }
}