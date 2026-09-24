package com.example.ride_service.service;

import com.example.ride_service.exception.ResourceNotFoundException;
import com.example.ride_service.model.Ride;
import com.example.ride_service.model.Ride.RideStatus;
import com.example.ride_service.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private RideService rideService;

    private Ride sampleRide;

    @BeforeEach
    void setUp() {
        sampleRide = new Ride(100L, "Central Station", "Airport", 35.50);
        sampleRide.setId("ride-123");
        sampleRide.setStatus(RideStatus.REQUESTED);
    }

    @Test
    void testCreateRide_Success() {
        Ride newRide = new Ride(100L, "Central Station", "Airport", 35.50);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride r = invocation.getArgument(0);
            r.setId("generated-id");
            return r;
        });

        Ride created = rideService.createRide(newRide);

        assertNotNull(created);
        assertEquals("generated-id", created.getId());
        assertEquals(RideStatus.REQUESTED, created.getStatus());
        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    @Test
    void testCreateRide_MissingRequiredFields_ThrowsException() {
        Ride invalidRide = new Ride();
        assertThrows(IllegalArgumentException.class, () -> rideService.createRide(invalidRide));
    }

    @Test
    void testAssignDriver_Success() {
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ride updated = rideService.assignDriver("ride-123", 200L);

        assertEquals(200L, updated.getDriverId());
        assertEquals(RideStatus.ASSIGNED, updated.getStatus());
        verify(rideRepository, times(1)).save(sampleRide);
    }

    @Test
    void testAssignDriver_InvalidStatus_ThrowsException() {
        sampleRide.setStatus(RideStatus.COMPLETED);
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));

        assertThrows(IllegalStateException.class, () -> rideService.assignDriver("ride-123", 200L));
    }

    @Test
    void testUpdateStatus_ValidTransitions() {
        sampleRide.setStatus(RideStatus.ASSIGNED);
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ride accepted = rideService.updateRideStatus("ride-123", RideStatus.ACCEPTED);
        assertEquals(RideStatus.ACCEPTED, accepted.getStatus());

        Ride inProgress = rideService.updateRideStatus("ride-123", RideStatus.IN_PROGRESS);
        assertEquals(RideStatus.IN_PROGRESS, inProgress.getStatus());

        Ride completed = rideService.updateRideStatus("ride-123", RideStatus.COMPLETED);
        assertEquals(RideStatus.COMPLETED, completed.getStatus());
    }

    @Test
    void testUpdateStatus_InvalidTransition_ThrowsException() {
        sampleRide.setStatus(RideStatus.REQUESTED);
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));

        assertThrows(IllegalStateException.class, () -> rideService.updateRideStatus("ride-123", RideStatus.COMPLETED));
    }

    @Test
    void testGetRideById_NotFound_ThrowsException() {
        when(rideRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rideService.getRideById("unknown"));
    }
}
