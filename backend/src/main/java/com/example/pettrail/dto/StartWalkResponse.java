package com.example.pettrail.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response for starting a walk")
public class StartWalkResponse {

    @Schema(description = "ID of the created walk", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID walkId;

    @Schema(description = "When the walk started (ISO-8601 format)", example = "2025-08-14T22:15:30Z")
    private String startedAt;

    // Constructors
    public StartWalkResponse() {}

    public StartWalkResponse(UUID walkId, String startedAt) {
        this.walkId = walkId;
        this.startedAt = startedAt;
    }

    // Getters and Setters
    public UUID getWalkId() {
        return walkId;
    }

    public void setWalkId(UUID walkId) {
        this.walkId = walkId;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }
}
