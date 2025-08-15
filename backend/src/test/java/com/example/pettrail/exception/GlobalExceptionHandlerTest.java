package com.example.pettrail.exception;

import com.example.pettrail.dto.ErrorResponse;
import com.example.pettrail.enums.ErrorCode;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationExceptions_ShouldReturn400WithValidationError() {
        // Given
        FieldError fieldError = new FieldError("pet", "name", "must not be blank");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                null, new org.springframework.validation.BeanPropertyBindingResult(new Object(), "pet")
        );
        ex.getBindingResult().addError(fieldError);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR, response.getBody().getCode());
        assertEquals("One or more validation errors occurred.", response.getBody().getMessage());
        assertEquals(1, response.getBody().getDetails().size());
        assertEquals("name", response.getBody().getDetails().get(0).getField());
        assertEquals("must not be blank", response.getBody().getDetails().get(0).getIssue());
    }

    @Test
    void handlePetNotFoundException_ShouldReturn404WithNotFoundError() {
        // Given
        PetNotFoundException ex = new PetNotFoundException("Pet not found");

        // When
        ResponseEntity<ErrorResponse> response = handler.handlePetNotFoundException(ex);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.NOT_FOUND, response.getBody().getCode());
        assertEquals("pet not found", response.getBody().getMessage());
        assertEquals(1, response.getBody().getDetails().size());
        assertEquals("petId", response.getBody().getDetails().get(0).getField());
        assertEquals("unknown", response.getBody().getDetails().get(0).getIssue());
    }

    @Test
    void handleActiveWalkExistsException_ShouldReturn409WithConflictError() {
        // Given
        ActiveWalkExistsException ex = new ActiveWalkExistsException("caminhada ativa já existe");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleActiveWalkExistsException(ex);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.CONFLICT, response.getBody().getCode());
        assertEquals("caminhada ativa já existe", response.getBody().getMessage());
        assertEquals(0, response.getBody().getDetails().size());
    }

    @Test
    void handleWalkNotFoundException_ShouldReturn404WithNotFoundError() {
        // Given
        WalkNotFoundException ex = new WalkNotFoundException("Walk not found");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleWalkNotFoundException(ex);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.NOT_FOUND, response.getBody().getCode());
        assertEquals("walk not found", response.getBody().getMessage());
        assertEquals(0, response.getBody().getDetails().size());
    }

    @Test
    void handleWalkFinishedException_ShouldReturn409WithConflictError() {
        // Given
        WalkFinishedException ex = new WalkFinishedException("walk already finished");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleWalkFinishedException(ex);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.CONFLICT, response.getBody().getCode());
        assertEquals("walk already finished", response.getBody().getMessage());
        assertEquals(0, response.getBody().getDetails().size());
    }

    @Test
    void handleMethodArgumentTypeMismatchException_ShouldReturn400WithValidationError() {
        // Given
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "invalid", Long.class, "petId", null, null
        );

        // When
        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatchException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR, response.getBody().getCode());
        assertEquals("Invalid query parameter.", response.getBody().getMessage());
        assertEquals(1, response.getBody().getDetails().size());
        assertEquals("petId", response.getBody().getDetails().get(0).getField());
        assertEquals("required numeric id", response.getBody().getDetails().get(0).getIssue());
    }

    @Test
    void handleGenericException_ShouldReturn500WithInternalError() {
        // Given
        Exception ex = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_ERROR, response.getBody().getCode());
        assertEquals("An unexpected error occurred.", response.getBody().getMessage());
        assertEquals(0, response.getBody().getDetails().size());
    }
}
