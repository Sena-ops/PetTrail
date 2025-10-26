package com.example.pettrail.controller;

import com.example.pettrail.dto.StartWalkResponse;
import com.example.pettrail.dto.StopWalkResponse;
import com.example.pettrail.dto.WalkPointRequest;
import com.example.pettrail.dto.WalkPointsBatchResponse;
import com.example.pettrail.dto.WalksPageResponse;
import com.example.pettrail.dto.WalkGeoJsonResponse;
import com.example.pettrail.exception.PaginationValidationException;
import com.example.pettrail.service.WalkService;
import com.example.pettrail.service.WalkPointsService;
import com.example.pettrail.validation.ValidWalkPointsArray;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/walks")
@Tag(name = "Walks", description = "Walk management endpoints")
public class WalkController {

    private final WalkService walkService;
    private final WalkPointsService walkPointsService;

    @Autowired
    public WalkController(WalkService walkService, WalkPointsService walkPointsService) {
        this.walkService = walkService;
        this.walkPointsService = walkPointsService;
    }

    @PostMapping("/start")
    @Operation(
        summary = "Start a walk for a pet",
        description = "Start a new walk for the specified pet. Only one active walk per pet is allowed."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Walk started successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StartWalkResponse.class),
                examples = @ExampleObject(
                    value = "{\"walkId\": 101, \"startedAt\": \"2025-08-14T22:15:30Z\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pet ID parameter",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Invalid Pet ID",
                    value = "{\"code\": \"VALIDATION_ERROR\", \"message\": \"Invalid query parameter.\", \"details\": [{\"field\": \"petId\", \"issue\": \"required numeric id\"}]}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pet not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Pet Not Found",
                    value = "{\"code\": \"NOT_FOUND\", \"message\": \"pet not found\", \"details\": [{\"field\": \"petId\", \"issue\": \"unknown\"}]}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Active walk already exists for this pet",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Active Walk Conflict",
                    value = "{\"code\": \"CONFLICT\", \"message\": \"caminhada ativa já existe\", \"details\": []}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Internal Error",
                    value = "{\"code\": \"INTERNAL_ERROR\", \"message\": \"An unexpected error occurred.\", \"details\": []}"
                )
            )
        )
    })
    public ResponseEntity<StartWalkResponse> startWalk(
            @Parameter(
                description = "ID of the pet to start a walk for",
                required = true,
                example = "42"
            )
            @RequestParam("petId") UUID petId) {
        
        StartWalkResponse response = walkService.startWalk(petId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(
        summary = "Get active walk for a pet",
        description = "Get the currently active walk for the specified pet, if any exists."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Active walk found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StartWalkResponse.class),
                examples = @ExampleObject(
                    value = "{\"walkId\": 101, \"startedAt\": \"2025-08-14T22:15:30Z\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No active walk found for this pet",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "No Active Walk",
                    value = "{\"code\": \"NOT_FOUND\", \"message\": \"No active walk found for this pet\", \"details\": []}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Internal Error",
                    value = "{\"code\": \"INTERNAL_ERROR\", \"message\": \"An unexpected error occurred.\", \"details\": []}"
                )
            )
        )
    })
    public ResponseEntity<StartWalkResponse> getActiveWalk(
            @Parameter(
                description = "ID of the pet to check for active walk",
                required = true,
                example = "42"
            )
            @RequestParam("petId") UUID petId) {
        
        StartWalkResponse response = walkService.getActiveWalk(petId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/points")
    @Operation(
        summary = "Upload walk points in batch",
        description = "Upload GPS points for a walk. Points are validated, sorted by timestamp, and outliers (speed > 50 m/s) are discarded. Coordinates must be WGS84 lat/lon in degrees for OpenStreetMap compatibility."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "202",
            description = "Points processed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WalkPointsBatchResponse.class),
                examples = @ExampleObject(
                    value = "{\"received\": 2, \"accepted\": 2, \"discarded\": 0}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid payload (empty array, too many points, or validation errors)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Invalid Payload",
                    value = "{\"code\": \"VALIDATION_ERROR\", \"message\": \"Payload must have 1..5000 points.\", \"details\": []}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Walk not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Walk Not Found",
                    value = "{\"code\": \"NOT_FOUND\", \"message\": \"walk not found\", \"details\": []}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Walk already finished",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Walk Already Finished",
                    value = "{\"code\": \"CONFLICT\", \"message\": \"walk already finished\", \"details\": []}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Internal Error",
                    value = "{\"code\": \"INTERNAL_ERROR\", \"message\": \"An unexpected error occurred.\", \"details\": []}"
                )
            )
        )
    })
    public ResponseEntity<WalkPointsBatchResponse> uploadWalkPoints(
            @Parameter(
                description = "ID of the walk to upload points for",
                required = true,
                example = "123"
            )
            @PathVariable("id") UUID walkId,
            @Parameter(
                description = "Array of GPS points (1-5000 points). Coordinates must be WGS84 lat/lon in degrees.",
                required = true
            )
            @Valid @ValidWalkPointsArray @RequestBody List<WalkPointRequest> points) {
        
        WalkPointsBatchResponse response = walkPointsService.ingestPoints(walkId, points);
        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/{id}/stop")
    @Operation(
        summary = "Stop a walk and compute consolidated metrics",
        description = "Stop an active walk, compute distance, duration, and average speed using WGS84 coordinates (OpenStreetMap compatible), and persist the metrics. Idempotent: subsequent calls return 409 if already finished."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Walk stopped successfully with consolidated metrics",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StopWalkResponse.class),
                examples = @ExampleObject(
                    value = "{\"walkId\": 123, \"distanciaM\": 2450.7, \"duracaoS\": 1560, \"velMediaKmh\": 5.65, \"startedAt\": \"2025-08-13T23:15:00Z\", \"finishedAt\": \"2025-08-13T23:41:00Z\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Walk not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Walk Not Found",
                    value = "{\"code\": \"NOT_FOUND\", \"message\": \"walk not found\", \"details\": [{\"field\": \"id\", \"issue\": \"unknown\"}]}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Walk already finished",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Walk Already Finished",
                    value = "{\"code\": \"CONFLICT\", \"message\": \"walk already finished\", \"details\": []}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Internal Error",
                    value = "{\"code\": \"INTERNAL_ERROR\", \"message\": \"An unexpected error occurred.\", \"details\": []}"
                )
            )
        )
    })
    public ResponseEntity<StopWalkResponse> stopWalk(
            @Parameter(
                description = "ID of the walk to stop",
                required = true,
                example = "123"
            )
            @PathVariable("id") UUID walkId) {
        
        StopWalkResponse response = walkService.stopWalk(walkId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
        summary = "List walks by pet with pagination",
        description = "Get a paginated list of walks for a specific pet, ordered by start time descending. Returns walks with their metrics (distance, duration, average speed)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Walks retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WalksPageResponse.class),
                examples = @ExampleObject(
                    value = "{\"content\": [{\"id\": 101, \"startedAt\": \"2025-08-13T23:15:00Z\", \"finishedAt\": \"2025-08-13T23:41:00Z\", \"distanciaM\": 2450.7, \"duracaoS\": 1560, \"velMediaKmh\": 5.65}], \"page\": 0, \"size\": 10, \"totalPages\": 3, \"totalElements\": 21}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Invalid Size",
                    value = "{\"code\": \"VALIDATION_ERROR\", \"message\": \"Invalid pagination parameters.\", \"details\": [{\"field\": \"size\", \"issue\": \"must be between 1 and 100\"}]}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pet not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Pet Not Found",
                    value = "{\"code\": \"NOT_FOUND\", \"message\": \"pet not found\", \"details\": [{\"field\": \"petId\", \"issue\": \"unknown\"}]}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Internal Error",
                    value = "{\"code\": \"INTERNAL_ERROR\", \"message\": \"An unexpected error occurred.\", \"details\": []}"
                )
            )
        )
    })
    public ResponseEntity<WalksPageResponse> listWalksByPet(
            @Parameter(
                description = "ID of the pet to list walks for",
                required = true,
                example = "42"
            )
            @RequestParam("petId") UUID petId,
            @Parameter(
                description = "Page number (zero-based, default: 0)",
                example = "0"
            )
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(
                description = "Page size (1-100, default: 10)",
                example = "10"
            )
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        // Validate pagination parameters
        if (page < 0) {
            throw new PaginationValidationException("Page must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new PaginationValidationException("Size must be between 1 and 100");
        }
        
        WalksPageResponse response = walkService.listByPet(petId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/geojson")
    @Operation(
        summary = "Get walk route as GeoJSON",
        description = "Get a walk's route as a GeoJSON Feature with LineString geometry. Coordinates are in WGS84 [longitude, latitude] order for OpenStreetMap/Leaflet compatibility. Returns empty LineString if walk has no points."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "GeoJSON Feature with walk route",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WalkGeoJsonResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Walk with points",
                        value = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[-46.6333,-23.5505],[-46.6339,-23.5510]]},\"properties\":{\"walkId\":123}}"
                    ),
                    @ExampleObject(
                        name = "Walk without points",
                        value = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[]},\"properties\":{\"walkId\":123}}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Walk not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Walk Not Found",
                    value = "{\"code\": \"NOT_FOUND\", \"message\": \"walk not found\", \"details\": [{\"field\": \"id\", \"issue\": \"unknown\"}]}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Internal Error",
                    value = "{\"code\": \"INTERNAL_ERROR\", \"message\": \"An unexpected error occurred.\", \"details\": []}"
                )
            )
        )
    })
    public ResponseEntity<WalkGeoJsonResponse> getWalkGeoJson(
            @Parameter(
                description = "ID of the walk to get GeoJSON for",
                required = true,
                example = "123"
            )
            @PathVariable("id") UUID walkId) {
        
        WalkGeoJsonResponse response = walkService.getGeoJson(walkId);
        return ResponseEntity.ok(response);
    }
}
