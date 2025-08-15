package com.example.pettrail.repository;

import com.example.pettrail.model.Walk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalkRepository extends JpaRepository<Walk, Long> {
    
    /**
     * Find an active walk for a specific pet
     * @param petId the pet ID
     * @return Optional containing the active walk if exists
     */
    @Query("SELECT w FROM Walk w WHERE w.petId = :petId AND w.finishedAt IS NULL")
    Optional<Walk> findActiveWalkByPetId(@Param("petId") Long petId);
    
    /**
     * Check if an active walk exists for a specific pet
     * @param petId the pet ID
     * @return true if an active walk exists, false otherwise
     */
    @Query("SELECT COUNT(w) > 0 FROM Walk w WHERE w.petId = :petId AND w.finishedAt IS NULL")
    boolean existsActiveWalkByPetId(@Param("petId") Long petId);
}
