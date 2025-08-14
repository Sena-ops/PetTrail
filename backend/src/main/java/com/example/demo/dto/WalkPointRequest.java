package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "GPS point data for walk tracking")
public class WalkPointRequest {

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90 degrees")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90 degrees")
    @Schema(description = "Latitude in WGS84 degrees (compatible with OpenStreetMap)", example = "-23.5505", required = true)
    private BigDecimal lat;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180 degrees")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180 degrees")
    @Schema(description = "Longitude in WGS84 degrees (compatible with OpenStreetMap)", example = "-46.6333", required = true)
    private BigDecimal lon;

    @NotNull(message = "Timestamp is required")
    @Schema(description = "ISO-8601 timestamp when this point was recorded", example = "2025-08-14T22:00:00Z", required = true)
    private LocalDateTime ts;

    @Schema(description = "Elevation in meters above sea level (optional)", example = "760.2")
    private BigDecimal elev;

    // Constructors
    public WalkPointRequest() {}

    public WalkPointRequest(BigDecimal lat, BigDecimal lon, LocalDateTime ts) {
        this.lat = lat;
        this.lon = lon;
        this.ts = ts;
    }

    public WalkPointRequest(BigDecimal lat, BigDecimal lon, LocalDateTime ts, BigDecimal elev) {
        this(lat, lon, ts);
        this.elev = elev;
    }

    // Getters and Setters
    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public void setLon(BigDecimal lon) {
        this.lon = lon;
    }

    public LocalDateTime getTs() {
        return ts;
    }

    public void setTs(LocalDateTime ts) {
        this.ts = ts;
    }

    public BigDecimal getElev() {
        return elev;
    }

    public void setElev(BigDecimal elev) {
        this.elev = elev;
    }
}
