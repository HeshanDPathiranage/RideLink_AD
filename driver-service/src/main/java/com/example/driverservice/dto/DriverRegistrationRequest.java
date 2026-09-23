package com.example.driverservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRegistrationRequest {
    private String name;
    private String licenseNumber;
    private String vehicleType;
    private String vehicleNumber;
    private boolean available;
    private String currentLocation;
}
