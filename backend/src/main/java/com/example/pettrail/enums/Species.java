package com.example.pettrail.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pet species enumeration")
@JsonDeserialize(using = SpeciesDeserializer.class)
public enum Species {
    @Schema(description = "Dog species")
    CACHORRO,
    
    @Schema(description = "Cat species")
    GATO;
    
    @JsonValue
    public String getValue() {
        return this.name();
    }
}
