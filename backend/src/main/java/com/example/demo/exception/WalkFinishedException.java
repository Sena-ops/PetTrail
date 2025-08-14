package com.example.demo.exception;

public class WalkFinishedException extends RuntimeException {
    
    public WalkFinishedException(String message) {
        super(message);
    }
    
    public WalkFinishedException(String message, Throwable cause) {
        super(message, cause);
    }
}
