package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "walk_points")
@Schema(description = "GPS point recorded during a walk")
public class WalkPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique ID of the walk point", example = "1")
    private Long id;

    @NotNull(message = "Walk ID is required")
    @Column(name = "walk_id", nullable = false)
    @Schema(description = "ID of the walk this point belongs to", example = "1", required = true)
    private Long walkId;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90 degrees")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90 degrees")
    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    @Schema(description = "Latitude in WGS84 degrees (compatible with OpenStreetMap)", example = "-23.5505", required = true)
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180 degrees")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180 degrees")
    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    @Schema(description = "Longitude in WGS84 degrees (compatible with OpenStreetMap)", example = "-46.6333", required = true)
    private BigDecimal longitude;

    @NotNull(message = "Timestamp is required")
    @Column(name = "timestamp", nullable = false)
    @Schema(description = "ISO-8601 timestamp when this point was recorded", example = "2025-08-14T22:00:00Z", required = true)
    private LocalDateTime timestamp;

    @Column(name = "elevation", precision = 8, scale = 2)
    @Schema(description = "Elevation in meters above sea level (optional)", example = "760.2")
    private BigDecimal elevation;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "When this point was stored in the database", example = "2025-08-14T22:00:00Z")
    private LocalDateTime createdAt;

    // Constructors
    public WalkPoint() {
        this.createdAt = LocalDateTime.now();
    }

    public WalkPoint(Long walkId, BigDecimal latitude, BigDecimal longitude, LocalDateTime timestamp) {
        this();
        this.walkId = walkId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public WalkPoint(Long walkId, BigDecimal latitude, BigDecimal longitude, LocalDateTime timestamp, BigDecimal elevation) {
        this(walkId, latitude, longitude, timestamp);
        this.elevation = elevation;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWalkId() {
        return walkId;
    }

    public void setWalkId(Long walkId) {
        this.walkId = walkId;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getElevation() {
        return elevation;
    }

    public void setElevation(BigDecimal elevation) {
        this.elevation = elevation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
