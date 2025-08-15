package com.example.pettrail.exception;

public class WalkNotFoundException extends RuntimeException {
    
    public WalkNotFoundException(String message) {
        super(message);
    }
    
    public WalkNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
