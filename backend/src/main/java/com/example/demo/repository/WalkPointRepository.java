package com.example.demo.repository;

import com.example.demo.model.WalkPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalkPointRepository extends JpaRepository<WalkPoint, Long> {
    
    /**
     * Find all points for a specific walk, ordered by timestamp
     * @param walkId the walk ID
     * @return List of walk points ordered by timestamp
     */
    @Query("SELECT wp FROM WalkPoint wp WHERE wp.walkId = :walkId ORDER BY wp.timestamp ASC")
    List<WalkPoint> findByWalkIdOrderByTimestamp(@Param("walkId") Long walkId);
    
    /**
     * Count points for a specific walk
     * @param walkId the walk ID
     * @return number of points for the walk
     */
    @Query("SELECT COUNT(wp) FROM WalkPoint wp WHERE wp.walkId = :walkId")
    long countByWalkId(@Param("walkId") Long walkId);
    
    /**
     * Delete all points for a specific walk
     * @param walkId the walk ID
     */
    @Query("DELETE FROM WalkPoint wp WHERE wp.walkId = :walkId")
    void deleteByWalkId(@Param("walkId") Long walkId);
}
