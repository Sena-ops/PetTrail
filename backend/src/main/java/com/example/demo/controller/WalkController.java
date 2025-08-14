package com.example.demo.controller;

import com.example.demo.dto.StartWalkResponse;
import com.example.demo.dto.StopWalkResponse;
import com.example.demo.dto.WalkPointRequest;
import com.example.demo.dto.WalkPointsBatchResponse;
import com.example.demo.service.WalkService;
import com.example.demo.service.WalkPointsService;
import com.example.demo.validation.ValidWalkPointsArray;
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
                schema = @Schema(implementation = com.example.demo.dto.ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"code\": \"VALIDATION_ERROR\", \"message\": \"Invalid query parameter.\", \"details\": [{\"field\": \"petId\", \"issue\": \"required numeric id\"}]}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pet not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.example.demo.dto.ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"code\": \"NOT_FOUND\", \"message\": \"pet not found\", \"details\": [{\"field\": \"petId\", \"issue\": \"unknown\"}]}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Active walk already exists for this pet",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.example.demo.dto.ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"code\": \"CONFLICT\", \"message\": \"caminhada ativa já existe\", \"details\": []}"
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
            @RequestParam("petId") Long petId) {
        
        StartWalkResponse response = walkService.startWalk(petId);
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
                schema = @Schema(implementation = com.example.demo.dto.ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"code\": \"BAD_REQUEST\", \"message\": \"Payload must have 1..5000 points.\", \"details\": []}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Walk not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.example.demo.dto.ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"code\": \"NOT_FOUND\", \"message\": \"walk not found\", \"details\": []}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Walk already finished",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.example.demo.dto.ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"code\": \"CONFLICT\", \"message\": \"walk already finished\", \"details\": []}"
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
            @PathVariable("id") Long walkId,
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
                schema = @Schema(implementation = com.example.demo.dto.ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"code\": \"NOT_FOUND\", \"message\": \"walk not found\", \"details\": [{\"field\": \"id\", \"issue\": \"unknown\"}]}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Walk already finished",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.example.demo.dto.ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"code\": \"CONFLICT\", \"message\": \"walk already finished\", \"details\": []}"
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
            @PathVariable("id") Long walkId) {
        
        StopWalkResponse response = walkService.stopWalk(walkId);
        return ResponseEntity.ok(response);
    }
}
