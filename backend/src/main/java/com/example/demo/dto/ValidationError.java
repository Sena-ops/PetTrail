package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Individual validation error details")
public class ValidationError {

    @Schema(description = "Field name that failed validation", example = "name")
    private String field;

    @Schema(description = "Description of the validation issue", example = "must not be blank")
    private String issue;

    // Constructors
    public ValidationError() {}

    public ValidationError(String field, String issue) {
        this.field = field;
        this.issue = issue;
    }

    // Getters and Setters
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }
}
