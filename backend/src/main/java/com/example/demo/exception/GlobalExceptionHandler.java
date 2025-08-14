package com.example.demo.exception;

import com.example.demo.dto.ErrorResponse;
import com.example.demo.dto.ValidationError;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapToValidationError)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "One or more validation errors occurred.",
                validationErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<ErrorResponse> handleJsonMappingException(JsonMappingException ex) {
        String message = ex.getMessage();
        String field = "species";
        String issue = "Invalid species value. Must be either CACHORRO or GATO";
        
        // Extract field name from the exception if possible
        if (message != null && message.contains("species")) {
            field = "species";
        }
        
        ValidationError validationError = new ValidationError(field, issue);
        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "One or more validation errors occurred.",
                List.of(validationError)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(PetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePetNotFoundException(PetNotFoundException ex) {
        ValidationError validationError = new ValidationError("petId", "unknown");
        ErrorResponse errorResponse = new ErrorResponse(
                "NOT_FOUND",
                "pet not found",
                List.of(validationError)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ActiveWalkExistsException.class)
    public ResponseEntity<ErrorResponse> handleActiveWalkExistsException(ActiveWalkExistsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "CONFLICT",
                ex.getMessage(),
                List.of()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String field = ex.getName();
        String issue = "required numeric id";
        
        ValidationError validationError = new ValidationError(field, issue);
        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "Invalid query parameter.",
                List.of(validationError)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    private ValidationError mapToValidationError(FieldError fieldError) {
        return new ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
