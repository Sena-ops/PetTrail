package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Standard error response format")
public class ErrorResponse {

    @Schema(description = "Error code", example = "VALIDATION_ERROR")
    private String code;

    @Schema(description = "Error message", example = "One or more validation errors occurred.")
    private String message;

    @Schema(description = "Detailed validation errors")
    private List<ValidationError> details;

    // Constructors
    public ErrorResponse() {}

    public ErrorResponse(String code, String message, List<ValidationError> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
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
