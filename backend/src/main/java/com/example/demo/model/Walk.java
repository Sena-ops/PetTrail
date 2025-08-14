package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "walks")
@Schema(description = "Entity that represents a walk")
public class Walk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique ID of the walk", example = "1")
    private Long id;

    @NotNull(message = "Pet ID is required")
    @Column(name = "pet_id", nullable = false)
    @Schema(description = "ID of the pet", example = "1", required = true)
    private Long petId;

    @NotNull(message = "Start time is required")
    @Column(name = "started_at", nullable = false)
    @Schema(description = "When the walk started", example = "2025-08-14T22:15:30Z", required = true)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    @Schema(description = "When the walk finished (null if still active)", example = "2025-08-14T23:15:30Z")
    private LocalDateTime finishedAt;

    // Constructors
    public Walk() {}

    public Walk(Long petId, LocalDateTime startedAt) {
        this.petId = petId;
        this.startedAt = startedAt;
        this.finishedAt = null;
    }

    public Walk(Long petId, LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.petId = petId;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
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

    public boolean isActive() {
        return finishedAt == null;
    }
}
