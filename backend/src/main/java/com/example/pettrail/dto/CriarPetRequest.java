package com.example.pettrail.dto;

import com.example.pettrail.enums.Species;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Schema(description = "Data for creating a new pet")
public class CriarPetRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 60, message = "Name must be between 1 and 60 characters")
    @Schema(description = "Name of the pet", example = "Rex", required = true, maxLength = 60)
    private String name;

    @NotNull(message = "Species is required")
    @Schema(description = "Species of the pet", example = "CACHORRO", required = true, allowableValues = {"CACHORRO", "GATO"})
    private Species species;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 30, message = "Age must be at most 30")
    @Schema(description = "Age of the pet in years", example = "5", required = true, minimum = "0", maximum = "30")
    private Integer age;

    @NotBlank(message = "Race is required")
    @Size(min = 1, max = 50, message = "Race must be between 1 and 50 characters")
    @Schema(description = "Race/breed of the pet", example = "Golden Retriever", required = true, maxLength = 50)
    private String race;

    // Constructors
    public CriarPetRequest() {}

    public CriarPetRequest(String name, Species species, Integer age, String race) {
        this.name = name;
        this.species = species;
        this.age = age;
        this.race = race;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }
}
