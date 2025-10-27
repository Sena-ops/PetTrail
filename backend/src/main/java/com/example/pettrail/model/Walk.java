package com.example.pettrail.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "walks")
@Schema(description = "Entity that represents a walk")
public class Walk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Unique ID of the walk", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotNull(message = "Pet ID is required")
    @Column(name = "pet_id", nullable = false)
    @Schema(description = "ID of the pet", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private UUID petId;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    @Schema(description = "ID of the user", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private UUID userId;

    @NotNull(message = "Start time is required")
    @Column(name = "started_at", nullable = false)
    @Schema(description = "When the walk started", example = "2025-08-14T22:15:30Z", required = true)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    @Schema(description = "When the walk finished (null if still active)", example = "2025-08-14T23:15:30Z")
    private LocalDateTime finishedAt;

    @Column(name = "distancia_m")
    @Schema(description = "Total distance in meters", example = "2450.7")
    private Double distanciaM;

    @Column(name = "duracao_s")
    @Schema(description = "Duration in seconds", example = "1560")
    private Integer duracaoS;

    @Column(name = "vel_media_kmh")
    @Schema(description = "Average speed in km/h", example = "5.65")
    private Double velMediaKmh;

    // Constructors
    public Walk() {}

    public Walk(UUID petId, UUID userId, LocalDateTime startedAt) {
        this.petId = petId;
        this.userId = userId;
        this.startedAt = startedAt;
        this.finishedAt = null;
        this.distanciaM = null;
        this.duracaoS = null;
        this.velMediaKmh = null;
    }

    public Walk(UUID petId, UUID userId, LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.petId = petId;
        this.userId = userId;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.distanciaM = null;
        this.duracaoS = null;
        this.velMediaKmh = null;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(UUID petId) {
        this.petId = petId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public boolean isActive() {
        return finishedAt == null;
    }
}
