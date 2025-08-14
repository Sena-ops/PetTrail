package com.example.demo.service;

import com.example.demo.dto.WalkPointRequest;
import com.example.demo.dto.WalkPointsBatchResponse;
import com.example.demo.exception.WalkFinishedException;
import com.example.demo.exception.WalkNotFoundException;
import com.example.demo.model.Walk;
import com.example.demo.model.WalkPoint;
import com.example.demo.repository.WalkPointRepository;
import com.example.demo.repository.WalkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class WalkPointsService {

    private static final Logger logger = LoggerFactory.getLogger(WalkPointsService.class);
    
    // Speed threshold in meters per second (50 m/s = 180 km/h)
    private static final double SPEED_THRESHOLD_MPS = 50.0;
    
    // Earth's radius in meters
    private static final double EARTH_RADIUS_M = 6371000.0;

    private final WalkRepository walkRepository;
    private final WalkPointRepository walkPointRepository;

    @Autowired
    public WalkPointsService(WalkRepository walkRepository, WalkPointRepository walkPointRepository) {
        this.walkRepository = walkRepository;
        this.walkPointRepository = walkPointRepository;
    }

    /**
     * Process a batch of walk points for a specific walk
     * @param walkId the walk ID
     * @param points the list of points to process
     * @return processing summary
     * @throws WalkNotFoundException if walk doesn't exist
     * @throws WalkFinishedException if walk is already finished
     */
    @Transactional
    public WalkPointsBatchResponse ingestPoints(Long walkId, List<WalkPointRequest> points) {
        // Validate walk exists and is active
        Walk walk = walkRepository.findById(walkId)
                .orElseThrow(() -> new WalkNotFoundException("Walk not found with ID: " + walkId));

        if (!walk.isActive()) {
            throw new WalkFinishedException("walk already finished");
        }

        int received = points.size();
        int accepted = 0;
        int discarded = 0;

        // Sort points by timestamp to ensure chronological order
        List<WalkPointRequest> sortedPoints = new ArrayList<>(points);
        sortedPoints.sort(Comparator.comparing(WalkPointRequest::getTs));

        List<WalkPoint> pointsToSave = new ArrayList<>();
        WalkPointRequest previousPoint = null;

        for (WalkPointRequest currentPoint : sortedPoints) {
            boolean shouldAccept = true;
            String discardReason = null;

            // Check if this is not the first point
            if (previousPoint != null) {
                // Calculate time difference
                Duration timeDiff = Duration.between(previousPoint.getTs(), currentPoint.getTs());
                long timeDiffSeconds = timeDiff.getSeconds();

                // If time difference is non-positive, discard the point
                if (timeDiffSeconds <= 0) {
                    shouldAccept = false;
                    discardReason = "non-increasing ts";
                } else {
                    // Calculate distance using Haversine formula
                    double distanceMeters = calculateHaversineDistance(
                            previousPoint.getLat().doubleValue(),
                            previousPoint.getLon().doubleValue(),
                            currentPoint.getLat().doubleValue(),
                            currentPoint.getLon().doubleValue()
                    );

                    // Calculate speed in meters per second
                    double speedMps = distanceMeters / timeDiffSeconds;

                    // If speed exceeds threshold, discard the point
                    if (speedMps > SPEED_THRESHOLD_MPS) {
                        shouldAccept = false;
                        discardReason = "speed > 50 m/s";
                    }
                }
            }

            if (shouldAccept) {
                // Convert to entity and add to save list
                WalkPoint walkPoint = new WalkPoint(
                        walkId,
                        currentPoint.getLat(),
                        currentPoint.getLon(),
                        currentPoint.getTs(),
                        currentPoint.getElev()
                );
                pointsToSave.add(walkPoint);
                accepted++;
                previousPoint = currentPoint;
            } else {
                discarded++;
                logger.info("Discarded point for walk {}: {} (lat: {}, lon: {}, ts: {})", 
                        walkId, discardReason, currentPoint.getLat(), currentPoint.getLon(), currentPoint.getTs());
            }
        }

        // Save all accepted points in batch
        if (!pointsToSave.isEmpty()) {
            walkPointRepository.saveAll(pointsToSave);
            logger.info("Saved {} points for walk {}", accepted, walkId);
        }

        logger.info("Walk points processing complete for walk {}: received={}, accepted={}, discarded={}", 
                walkId, received, accepted, discarded);

        return new WalkPointsBatchResponse(received, accepted, discarded);
    }

    /**
     * Calculate the distance between two points using the Haversine formula
     * @param lat1 latitude of first point in degrees
     * @param lon1 longitude of first point in degrees
     * @param lat2 latitude of second point in degrees
     * @param lon2 longitude of second point in degrees
     * @return distance in meters
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Differences in coordinates
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distance in meters
        return EARTH_RADIUS_M * c;
    }
}
