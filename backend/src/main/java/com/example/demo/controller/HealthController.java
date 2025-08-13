package com.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Endpoints para verificação de saúde da aplicação")
public class HealthController {

    @GetMapping
    @Operation(
        summary = "Verificar status da aplicação",
        description = "Retorna informações sobre o status atual da aplicação PatTrail"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Aplicação funcionando normalmente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "PatTrail");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Aplicação funcionando normalmente");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    @Operation(
        summary = "Ping simples",
        description = "Endpoint simples para verificar se a aplicação está respondendo"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pong retornado com sucesso",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "pong")
            )
        )
    })
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
