package com.example.pettrail.dto;

import com.example.pettrail.enums.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Standard error response format")
public class ErrorResponse {

    @Schema(
        description = "Error code", 
        example = "VALIDATION_ERROR",
        allowableValues = {"VALIDATION_ERROR", "NOT_FOUND", "CONFLICT", "INTERNAL_ERROR"}
    )
    private ErrorCode code;

    @Schema(description = "Human-readable error message", example = "One or more validation errors occurred.")
    private String message;

    @Schema(description = "Optional array with field-level validation issues")
    private List<ValidationError> details;

    // Constructors
    public ErrorResponse() {}

    public ErrorResponse(ErrorCode code, String message, List<ValidationError> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    // Getters and Setters
    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ValidationError> getDetails() {
        return details;
    }

    public void setDetails(List<ValidationError> details) {
        this.details = details;
    }
}
