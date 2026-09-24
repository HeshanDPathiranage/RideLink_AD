package com.example.driverservice.service;

import com.example.driverservice.dto.DriverRegistrationRequest;
import com.example.driverservice.exception.ResourceNotFoundException;
import com.example.driverservice.model.Driver;
import com.example.driverservice.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    /**
     * Register a new driver in the platform.
     *
     * @param request driver registration details
     * @return saved Driver entity
     */
    public Driver registerDriver(DriverRegistrationRequest request) {
        log.info("Registering new driver with license: {}", request.getLicenseNumber());

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Driver name cannot be empty");
        }
        if (request.getLicenseNumber() == null || request.getLicenseNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("License number cannot be empty");
        }
        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("Driver with license number " + request.getLicenseNumber() + " already exists");
        }

        Driver driver = Driver.builder()
                .name(request.getName())
                .licenseNumber(request.getLicenseNumber())
                .vehicleType(request.getVehicleType())
                .vehicleNumber(request.getVehicleNumber())
                .available(request.isAvailable())
                .currentLocation(request.getCurrentLocation())
                .build();

        return driverRepository.save(driver);
    }

    /**
     * Update a driver's availability status.
     *
     * @param driverId ID of the driver
     * @param available new availability status
     * @return updated Driver entity
     */
    public Driver updateAvailability(String driverId, boolean available) {
        log.info("Updating availability for driver ID: {} to {}", driverId, available);

        Driver driver = getDriverById(driverId);
        driver.setAvailable(available);

        return driverRepository.save(driver);
    }

    /**
     * Update a driver's current location.
     *
     * @param driverId ID of the driver
     * @param currentLocation new location name / coordinate string
     * @return updated Driver entity
     */
    public Driver updateLocation(String driverId, String currentLocation) {
        log.info("Updating location for driver ID: {} to {}", driverId, currentLocation);

        if (currentLocation == null || currentLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Current location cannot be empty");
        }

        Driver driver = getDriverById(driverId);
        driver.setCurrentLocation(currentLocation);

        return driverRepository.save(driver);
    }

    /**
     * Retrieve a list of available drivers in a specific location.
     *
     * @param location location to filter by
     * @return list of available drivers
     */
    public List<Driver> getAvailableDriversByLocation(String location) {
        log.info("Fetching available drivers for location: {}", location);

        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location parameter cannot be empty");
        }

        return driverRepository.findByAvailableTrueAndCurrentLocation(location);
    }

    /**
     * Get a single driver by their ID.
     *
     * @param driverId driver ID
     * @return Driver entity
     */
    public Driver getDriverById(String driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + driverId));
    }

    /**
     * Get all drivers in the system.
     *
     * @return list of all drivers
     */
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    /**
     * Delete a driver record by ID.
     *
     * @param driverId driver ID
     */
    public void deleteDriver(String driverId) {
        Driver driver = getDriverById(driverId);
        driverRepository.delete(driver);
    }
}
