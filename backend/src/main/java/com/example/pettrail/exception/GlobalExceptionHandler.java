package com.example.pettrail.exception;

import com.example.pettrail.dto.ErrorResponse;
import com.example.pettrail.dto.ValidationError;
import com.example.pettrail.enums.ErrorCode;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapToValidationError)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                "One or more validation errors occurred.",
                validationErrors
        );

        logger.warn("Validation error occurred: {}", errorResponse);
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
                ErrorCode.VALIDATION_ERROR,
                "One or more validation errors occurred.",
                List.of(validationError)
        );

        logger.warn("JSON mapping error occurred: {}", errorResponse);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(PetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePetNotFoundException(PetNotFoundException ex) {
        ValidationError validationError = new ValidationError("petId", "unknown");
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.NOT_FOUND,
                "pet not found",
                List.of(validationError)
        );

        logger.warn("Pet not found: {}", errorResponse);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ActiveWalkExistsException.class)
    public ResponseEntity<ErrorResponse> handleActiveWalkExistsException(ActiveWalkExistsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.CONFLICT,
                ex.getMessage(),
                List.of()
        );

        logger.warn("Active walk conflict: {}", errorResponse);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(WalkNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalkNotFoundException(WalkNotFoundException ex) {
        ValidationError validationError = new ValidationError("id", "unknown");
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.NOT_FOUND,
                "walk not found",
                List.of(validationError)
        );

        logger.warn("Walk not found: {}", errorResponse);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(WalkFinishedException.class)
    public ResponseEntity<ErrorResponse> handleWalkFinishedException(WalkFinishedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.CONFLICT,
                ex.getMessage(),
                List.of()
        );

        logger.warn("Walk finished conflict: {}", errorResponse);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                "Payload must have 1..5000 points.",
                List.of()
        );

        logger.warn("Constraint violation: {}", errorResponse);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String field = ex.getName();
        String issue = "required numeric id";
        
        ValidationError validationError = new ValidationError(field, issue);
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                "Invalid query parameter.",
                List.of(validationError)
        );

        logger.warn("Method argument type mismatch: {}", errorResponse);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String field = ex.getParameterName();
        String issue = "required";
        
        ValidationError validationError = new ValidationError(field, issue);
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                "Missing required parameter.",
                List.of(validationError)
        );

        logger.warn("Missing request parameter: {}", errorResponse);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(PaginationValidationException.class)
    public ResponseEntity<ErrorResponse> handlePaginationValidationException(PaginationValidationException ex) {
        String message = ex.getMessage();
        String field = "pagination";
        String issue = "invalid parameters";
        
        // Parse the message to determine which field failed validation
        if (message.contains("Page must be")) {
            field = "page";
            issue = "must be >= 0";
        } else if (message.contains("Size must be")) {
            field = "size";
            issue = "must be between 1 and 100";
        }
        
        ValidationError validationError = new ValidationError(field, issue);
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                "Invalid pagination parameters.",
                List.of(validationError)
        );

        logger.warn("Pagination validation error: {}", errorResponse);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INTERNAL_ERROR,
                "An unexpected error occurred.",
                List.of()
        );

        logger.error("Unexpected error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private ValidationError mapToValidationError(FieldError fieldError) {
        return new ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
