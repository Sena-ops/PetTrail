package com.example.pettrail.exception;

public class PaginationValidationException extends RuntimeException {
    
    public PaginationValidationException(String message) {
        super(message);
    }
    
    public PaginationValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
