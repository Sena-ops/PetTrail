package com.example.pettrail.controller;

import com.example.pettrail.dto.AuthResponse;
import com.example.pettrail.dto.LoginRequest;
import com.example.pettrail.dto.RegisterRequest;
import com.example.pettrail.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API for user authentication and registration")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with email, password, first name, and last name"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error or email already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                    {
                      "code": "VALIDATION_ERROR",
                      "message": "One or more validation errors occurred.",
                      "details": [
                        { "field": "email", "issue": "must be a valid email" },
                        { "field": "password", "issue": "must be at least 8 characters" }
                      ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Email Already Exists",
                    value = """
                    {
                      "code": "CONFLICT",
                      "message": "Email already exists",
                      "details": []
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<AuthResponse> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Email already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw e;
        }
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login user",
        description = "Authenticates a user with email and password, returns JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse")
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(
                    name = "Invalid Credentials",
                    value = """
                    {
                      "code": "UNAUTHORIZED",
                      "message": "Invalid email or password",
                      "details": []
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}


