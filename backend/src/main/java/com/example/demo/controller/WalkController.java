package com.example.demo.controller;

import com.example.demo.dto.StartWalkResponse;
import com.example.demo.service.WalkService;
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

@RestController
@RequestMapping("/api/walks")
@Tag(name = "Walks", description = "Walk management endpoints")
public class WalkController {

    private final WalkService walkService;

    @Autowired
    public WalkController(WalkService walkService) {
        this.walkService = walkService;
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
}
