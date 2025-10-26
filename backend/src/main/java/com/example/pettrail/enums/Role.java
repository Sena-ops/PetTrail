package com.example.pettrail.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User roles in the system")
public enum Role {
    @Schema(description = "Regular user with limited access")
    USER,
    
    @Schema(description = "Administrator with full access")
    ADMIN
}


