package com.example.demo.controller;

import com.example.demo.dto.CriarPetRequest;
import com.example.demo.dto.AtualizarPetRequest;
import com.example.demo.dto.ErrorResponse;
import com.example.demo.model.Pet;
import com.example.demo.repository.PetRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "Pets", description = "API for pet management")
public class PetController {

    @Autowired
    private PetRepository petRepository;

    @GetMapping
    @Operation(
        summary = "List all pets",
        description = "Returns a list of all pets with id, name, species, age, and race"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of pets returned successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Pet.class)
            )
        )
    })
    public ResponseEntity<List<Pet>> listPets() {
        List<Pet> pets = petRepository.findAll();
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get pet by ID",
        description = "Returns a specific pet by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pet found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Pet.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pet not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Pet> getPetById(
        @Parameter(description = "ID of the pet to retrieve", required = true)
        @PathVariable Long id
    ) {
        Optional<Pet> pet = petRepository.findById(id);
        if (pet.isPresent()) {
            return ResponseEntity.ok(pet.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(
        summary = "Create a new pet",
        description = "Creates a new pet with required fields: name (1-60 characters), species (CACHORRO or GATO), age (0-30), and race (1-50 characters). Duplicate pet names are permitted in this MVP."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Pet created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Pet.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Validation Error Example",
                    value = """
                    {
                      "code": "VALIDATION_ERROR",
                      "message": "One or more validation errors occurred.",
                      "details": [
                        { "field": "name", "issue": "must not be blank" }
                      ]
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Pet> createPet(
        @Parameter(description = "Pet data to create", required = true)
        @Valid @RequestBody CriarPetRequest request
    ) {
        Pet pet = new Pet(request.getName(), request.getSpecies(), request.getAge(), request.getRace());
        Pet savedPet = petRepository.save(pet);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPet);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update a pet",
        description = "Updates an existing pet's name, species, age, or race. All fields are optional - only provided fields will be updated."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pet updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Pet.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pet not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Pet> updatePet(
        @Parameter(description = "ID of the pet to update", required = true)
        @PathVariable Long id,
        @Parameter(description = "Pet data to update", required = true)
        @Valid @RequestBody AtualizarPetRequest request
    ) {
        Optional<Pet> existingPet = petRepository.findById(id);
        if (existingPet.isPresent()) {
            Pet pet = existingPet.get();
            
            if (request.getName() != null) {
                pet.setName(request.getName());
            }
            if (request.getSpecies() != null) {
                pet.setSpecies(request.getSpecies());
            }
            if (request.getAge() != null) {
                pet.setAge(request.getAge());
            }
            if (request.getRace() != null) {
                pet.setRace(request.getRace());
            }
            
            Pet updatedPet = petRepository.save(pet);
            return ResponseEntity.ok(updatedPet);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a pet",
        description = "Deletes a pet by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Pet deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pet not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Void> deletePet(
        @Parameter(description = "ID of the pet to delete", required = true)
        @PathVariable Long id
    ) {
        Optional<Pet> pet = petRepository.findById(id);
        if (pet.isPresent()) {
            petRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
