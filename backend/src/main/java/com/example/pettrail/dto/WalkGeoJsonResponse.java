package com.example.pettrail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "GeoJSON Feature representing a walk route as a LineString")
public class WalkGeoJsonResponse {

    @Schema(description = "GeoJSON type - always 'Feature'", example = "Feature", required = true)
    private String type = "Feature";

    @Schema(description = "GeoJSON geometry object", required = true)
    private Geometry geometry;

    @Schema(description = "GeoJSON properties object", required = true)
    private Properties properties;

    // Constructors
    public WalkGeoJsonResponse() {}

    public WalkGeoJsonResponse(UUID walkId, List<List<Double>> coordinates) {
        this.geometry = new Geometry(coordinates);
        this.properties = new Properties(walkId);
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Schema(description = "GeoJSON geometry object")
    public static class Geometry {
        @Schema(description = "GeoJSON geometry type - always 'LineString'", example = "LineString", required = true)
        private String type = "LineString";

        @Schema(description = "Array of coordinate pairs in [longitude, latitude] order (WGS84)", 
                example = "[[-46.6333, -23.5505], [-46.6339, -23.5510]]", required = true)
        private List<List<Double>> coordinates;

        // Constructors
        public Geometry() {}

        public Geometry(List<List<Double>> coordinates) {
            this.coordinates = coordinates;
        }

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<List<Double>> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<List<Double>> coordinates) {
            this.coordinates = coordinates;
        }
    }

    @Schema(description = "GeoJSON properties object")
    public static class Properties {
        @Schema(description = "ID of the walk", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
        private UUID walkId;

        // Constructors
        public Properties() {}

        public Properties(UUID walkId) {
            this.walkId = walkId;
        }

        // Getters and Setters
        public UUID getWalkId() {
            return walkId;
        }

        public void setWalkId(UUID walkId) {
            this.walkId = walkId;
        }
    }
}
