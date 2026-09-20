package com.example.fare_service.exception;

/**
 * Exception thrown when a requested resource (e.g. payment or receipt) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
