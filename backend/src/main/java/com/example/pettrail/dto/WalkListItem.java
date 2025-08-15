package com.example.pettrail.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Walk item in paginated list response")
public class WalkListItem {

    @Schema(description = "Unique ID of the walk", example = "1")
    private Long id;

    @Schema(description = "When the walk started", example = "2025-08-13T23:15:00Z")
    private LocalDateTime startedAt;

    @Schema(description = "When the walk finished (null if still active)", example = "2025-08-13T23:41:00Z")
    private LocalDateTime finishedAt;

    @Schema(description = "Total distance in meters", example = "2450.7")
    private Double distanciaM;

    @Schema(description = "Duration in seconds", example = "1560")
    private Integer duracaoS;

    @Schema(description = "Average speed in km/h", example = "5.65")
    private Double velMediaKmh;

    // Constructors
    public WalkListItem() {}

    public WalkListItem(Long id, LocalDateTime startedAt, LocalDateTime finishedAt, 
                       Double distanciaM, Integer duracaoS, Double velMediaKmh) {
        this.id = id;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.distanciaM = distanciaM;
        this.duracaoS = duracaoS;
        this.velMediaKmh = velMediaKmh;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
