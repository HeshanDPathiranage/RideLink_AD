package com.example.driverservice.controller;

import com.example.driverservice.dto.DriverRegistrationRequest;
import com.example.driverservice.model.Driver;
import com.example.driverservice.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DriverController {

    private final DriverService driverService;

    /**
     * Register a new driver.
     * POST /api/v1/drivers
     */
    @PostMapping
    public ResponseEntity<Driver> registerDriver(@RequestBody DriverRegistrationRequest request) {
        Driver createdDriver = driverService.registerDriver(request);
        return new ResponseEntity<>(createdDriver, HttpStatus.CREATED);
    }

    /**
     * Retrieve all drivers or filter available drivers by location.
     * GET /api/v1/drivers
     * GET /api/v1/drivers?location=Colombo
     */
    @GetMapping
    public ResponseEntity<List<Driver>> getAllDrivers(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean available) {
        if (Boolean.TRUE.equals(available) && location != null && !location.isBlank()) {
            return ResponseEntity.ok(driverService.getAvailableDriversByLocation(location));
        }
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    /**
     * Retrieve a list of available drivers in a specific location.
     * GET /api/v1/drivers/available?location=Colombo
     */
    @GetMapping("/available")
    public ResponseEntity<List<Driver>> getAvailableDriversByLocation(@RequestParam String location) {
        List<Driver> drivers = driverService.getAvailableDriversByLocation(location);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get a driver by their ID.
     * GET /api/v1/drivers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Driver> getDriverById(@PathVariable String id) {
        Driver driver = driverService.getDriverById(id);
        return ResponseEntity.ok(driver);
    }

    /**
     * Update a driver's availability status.
     * PUT /api/v1/drivers/{id}/availability?available=true
     * OR via JSON body: {"available": true}
     */
    @PutMapping("/{id}/availability")
    public ResponseEntity<Driver> updateAvailability(
            @PathVariable String id,
            @RequestParam(required = false) Boolean available,
            @RequestBody(required = false) Map<String, Boolean> body) {

        boolean isAvailable;
        if (available != null) {
            isAvailable = available;
        } else if (body != null && body.containsKey("available")) {
            isAvailable = body.get("available");
        } else {
            throw new IllegalArgumentException("Availability value is required");
        }

        Driver updatedDriver = driverService.updateAvailability(id, isAvailable);
        return ResponseEntity.ok(updatedDriver);
    }

    /**
     * Update a driver's current location.
     * PUT /api/v1/drivers/{id}/location?location=Colombo
     * OR via JSON body: {"currentLocation": "Colombo"}
     */
    @PutMapping("/{id}/location")
    public ResponseEntity<Driver> updateLocation(
            @PathVariable String id,
            @RequestParam(required = false) String location,
            @RequestBody(required = false) Map<String, String> body) {

        String targetLocation;
        if (location != null && !location.isBlank()) {
            targetLocation = location;
        } else if (body != null && body.containsKey("currentLocation")) {
            targetLocation = body.get("currentLocation");
        } else if (body != null && body.containsKey("location")) {
            targetLocation = body.get("location");
        } else {
            throw new IllegalArgumentException("Location value is required");
        }

        Driver updatedDriver = driverService.updateLocation(id, targetLocation);
        return ResponseEntity.ok(updatedDriver);
    }

    /**
     * Delete a driver record.
     * DELETE /api/v1/drivers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable String id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }
}
