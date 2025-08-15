package com.example.pettrail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Batch request for uploading walk points")
public class WalkPointsBatchRequest {

    @NotEmpty(message = "Points array cannot be empty")
    @Size(max = 5000, message = "Maximum 5000 points allowed per batch")
    @Valid
    @Schema(description = "Array of GPS points (1-5000 points)", required = true)
    private List<WalkPointRequest> points;

    // Constructors
    public WalkPointsBatchRequest() {}

    public WalkPointsBatchRequest(List<WalkPointRequest> points) {
        this.points = points;
    }

    // Getters and Setters
    public List<WalkPointRequest> getPoints() {
        return points;
    }

    public void setPoints(List<WalkPointRequest> points) {
        this.points = points;
    }
}
