package com.example.pettrail.exception;

public class ActiveWalkExistsException extends RuntimeException {
    
    public ActiveWalkExistsException(String message) {
        super(message);
    }
    
    public ActiveWalkExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
