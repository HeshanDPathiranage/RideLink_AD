package com.example.driverservice.repository;

import com.example.driverservice.model.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends MongoRepository<Driver, String> {

    /**
     * Find drivers who are currently available in a specific location.
     *
     * @param currentLocation current location name/zone
     * @return list of matching drivers
     */
    List<Driver> findByAvailableTrueAndCurrentLocation(String currentLocation);

    /**
     * Check if a driver with a given license number already exists.
     */
    boolean existsByLicenseNumber(String licenseNumber);

    /**
     * Check if a driver with a given vehicle number already exists.
     */
    boolean existsByVehicleNumber(String vehicleNumber);
}
