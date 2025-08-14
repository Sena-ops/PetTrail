package com.example.demo.model;

import com.example.demo.enums.Species;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;

@Entity
@Table(name = "pets")
@Schema(description = "Entity that represents a pet")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique ID of the pet", example = "1")
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 60, message = "Name must be between 1 and 60 characters")
    @Column(nullable = false)
    @Schema(description = "Name of the pet", example = "Rex", required = true, maxLength = 60)
    private String name;

    @NotNull(message = "Species is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Species of the pet", example = "CACHORRO", required = true, allowableValues = {"CACHORRO", "GATO"})
    private Species species;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 30, message = "Age must be at most 30")
    @Column(nullable = false)
    @Schema(description = "Age of the pet in years", example = "5", required = true, minimum = "0", maximum = "30")
    private Integer age;

    @NotBlank(message = "Race is required")
    @Size(min = 1, max = 50, message = "Race must be between 1 and 50 characters")
    @Column(nullable = false)
    @Schema(description = "Race/breed of the pet", example = "Golden Retriever", required = true, maxLength = 50)
    private String race;

    @Column(name = "created_at")
    @Schema(description = "Creation timestamp of the pet")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public Pet() {}

    public Pet(String name, Species species, Integer age, String race) {
        this.name = name;
        this.species = species;
        this.age = age;
        this.race = race;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
