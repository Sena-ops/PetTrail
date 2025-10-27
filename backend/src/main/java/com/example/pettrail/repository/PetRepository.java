package com.example.pettrail.repository;

import com.example.pettrail.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PetRepository extends JpaRepository<Pet, UUID> {
    
    /**
     * Find all pets belonging to a specific user
     * @param userId the ID of the user
     * @return List of pets belonging to the user
     */
    List<Pet> findByUserId(UUID userId);
    
    /**
     * Find a pet by ID and user ID to ensure user can only access their own pets
     * @param id the pet ID
     * @param userId the user ID
     * @return Optional containing the pet if found and belongs to user
     */
    Optional<Pet> findByIdAndUserId(UUID id, UUID userId);
    
    /**
     * Check if a pet exists with the given ID and belongs to the given user
     * @param id the pet ID
     * @param userId the user ID
     * @return true if pet exists and belongs to user, false otherwise
     */
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
