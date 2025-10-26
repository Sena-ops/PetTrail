package com.example.pettrail.repository;

import com.example.pettrail.model.Walk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalkRepository extends JpaRepository<Walk, UUID> {
    
    /**
     * Find an active walk for a specific pet
     * @param petId the pet ID
     * @return Optional containing the active walk if exists
     */
    @Query("SELECT w FROM Walk w WHERE w.petId = :petId AND w.finishedAt IS NULL")
    Optional<Walk> findActiveWalkByPetId(@Param("petId") UUID petId);
    
    /**
     * Check if an active walk exists for a specific pet
     * @param petId the pet ID
     * @return true if an active walk exists, false otherwise
     */
    @Query("SELECT COUNT(w) > 0 FROM Walk w WHERE w.petId = :petId AND w.finishedAt IS NULL")
    boolean existsActiveWalkByPetId(@Param("petId") UUID petId);
    
    /**
     * Find all walks for a specific pet, ordered by start time descending
     * @param petId the pet ID
     * @param pageable pagination parameters
     * @return Page of walks
     */
    @Query("SELECT w FROM Walk w WHERE w.petId = :petId ORDER BY w.startedAt DESC")
    Page<Walk> findByPetIdOrderByStartedAtDesc(@Param("petId") UUID petId, Pageable pageable);
    
    /**
     * Count total walks for a specific pet
     * @param petId the pet ID
     * @return total number of walks
     */
    @Query("SELECT COUNT(w) FROM Walk w WHERE w.petId = :petId")
    long countByPetId(@Param("petId") UUID petId);
}
