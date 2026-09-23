package com.example.driverservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "drivers")
public class Driver {

    @Id
    private String id;
    private String name;
    private String licenseNumber;
    private String vehicleType;
    private String vehicleNumber;
    private boolean available;
    private String currentLocation;
}
