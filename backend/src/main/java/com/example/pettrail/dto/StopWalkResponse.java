package com.example.pettrail.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response for stopping a walk with consolidated metrics")
public class StopWalkResponse {

    @Schema(description = "ID of the walk", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private UUID walkId;

    @Schema(description = "Total distance in meters", example = "2450.7", required = true)
    private Double distanciaM;

    @Schema(description = "Duration in seconds", example = "1560", required = true)
    private Integer duracaoS;

    @Schema(description = "Average speed in km/h", example = "5.65", required = true)
    private Double velMediaKmh;

    @Schema(description = "When the walk started", example = "2025-08-13T23:15:00Z", required = true)
    private LocalDateTime startedAt;

    @Schema(description = "When the walk finished", example = "2025-08-13T23:41:00Z", required = true)
    private LocalDateTime finishedAt;

    // Constructors
    public StopWalkResponse() {}

    public StopWalkResponse(UUID walkId, Double distanciaM, Integer duracaoS, Double velMediaKmh, 
                           LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.walkId = walkId;
        this.distanciaM = distanciaM;
        this.duracaoS = duracaoS;
        this.velMediaKmh = velMediaKmh;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    // Getters and Setters
    public UUID getWalkId() {
        return walkId;
    }

    public void setWalkId(UUID walkId) {
        this.walkId = walkId;
    }

    public Double getDistanciaM() {
        return distanciaM;
    }

    public void setDistanciaM(Double distanciaM) {
        this.distanciaM = distanciaM;
    }

    public Integer getDuracaoS() {
        return duracaoS;
    }

    public void setDuracaoS(Integer duracaoS) {
        this.duracaoS = duracaoS;
    }

    public Double getVelMediaKmh() {
        return velMediaKmh;
    }

    public void setVelMediaKmh(Double velMediaKmh) {
        this.velMediaKmh = velMediaKmh;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
