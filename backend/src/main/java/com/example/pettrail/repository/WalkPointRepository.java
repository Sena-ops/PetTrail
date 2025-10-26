package com.example.pettrail.repository;

import com.example.pettrail.model.WalkPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalkPointRepository extends JpaRepository<WalkPoint, UUID> {
    
    /**
     * Find all points for a specific walk, ordered by timestamp
     * @param walkId the walk ID
     * @return List of walk points ordered by timestamp
     */
    @Query("SELECT wp FROM WalkPoint wp WHERE wp.walkId = :walkId ORDER BY wp.timestamp ASC")
    List<WalkPoint> findByWalkIdOrderByTimestamp(@Param("walkId") UUID walkId);
    
    /**
     * Count points for a specific walk
     * @param walkId the walk ID
     * @return number of points for the walk
     */
    @Query("SELECT COUNT(wp) FROM WalkPoint wp WHERE wp.walkId = :walkId")
    long countByWalkId(@Param("walkId") UUID walkId);
    
    /**
     * Delete all points for a specific walk
     * @param walkId the walk ID
     */
    @Query("DELETE FROM WalkPoint wp WHERE wp.walkId = :walkId")
    void deleteByWalkId(@Param("walkId") UUID walkId);
}
