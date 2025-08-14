package com.example.demo.enums;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;

public class SpeciesDeserializer extends JsonDeserializer<Species> {

    @Override
    public Species deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        
        if (value == null || value.trim().isEmpty()) {
            throw new JsonMappingException(p, "Species cannot be null or empty");
        }
        
        try {
            return Species.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new JsonMappingException(p, "Invalid species value: " + value + ". Must be either CACHORRO or GATO");
        }
    }
}
