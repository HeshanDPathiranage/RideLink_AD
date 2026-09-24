package com.example.ride_service.controller;

import com.example.ride_service.model.Ride;
import com.example.ride_service.model.Ride.RideStatus;
import com.example.ride_service.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
public class RideController {

    private final RideService rideService;

    @Autowired
    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    public ResponseEntity<Ride> createRide(@RequestBody Ride ride) {
        Ride createdRide = rideService.createRide(ride);
        return new ResponseEntity<>(createdRide, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<Ride> assignDriver(@PathVariable String id, @RequestParam Long driverId) {
        Ride assignedRide = rideService.assignDriver(id, driverId);
        return ResponseEntity.ok(assignedRide);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Ride> updateStatus(@PathVariable String id, @RequestParam RideStatus status) {
        Ride updatedRide = rideService.updateRideStatus(id, status);
        return ResponseEntity.ok(updatedRide);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ride> getRideById(@PathVariable String id) {
        Ride ride = rideService.getRideById(id);
        return ResponseEntity.ok(ride);
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<Ride>> getPassengerRides(@PathVariable Long passengerId) {
        List<Ride> rides = rideService.getRidesByPassenger(passengerId);
        return ResponseEntity.ok(rides);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Ride>> getDriverRides(@PathVariable Long driverId) {
        List<Ride> rides = rideService.getRidesByDriver(driverId);
        return ResponseEntity.ok(rides);
    }

    @GetMapping
    public ResponseEntity<List<Ride>> getAllRides() {
        List<Ride> rides = rideService.getAllRides();
        return ResponseEntity.ok(rides);
    }
}