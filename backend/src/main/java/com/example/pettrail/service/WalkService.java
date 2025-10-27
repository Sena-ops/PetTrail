package com.example.pettrail.service;

import com.example.pettrail.dto.StartWalkResponse;
import com.example.pettrail.dto.StopWalkResponse;
import com.example.pettrail.dto.WalksPageResponse;
import com.example.pettrail.dto.WalkListItem;
import com.example.pettrail.dto.WalkGeoJsonResponse;
import com.example.pettrail.exception.PetNotFoundException;
import com.example.pettrail.exception.ActiveWalkExistsException;
import com.example.pettrail.exception.WalkNotFoundException;
import com.example.pettrail.exception.WalkFinishedException;
import com.example.pettrail.model.Walk;
import com.example.pettrail.model.WalkPoint;
import com.example.pettrail.model.User;
import com.example.pettrail.repository.PetRepository;
import com.example.pettrail.repository.WalkRepository;
import com.example.pettrail.repository.WalkPointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WalkService {

    private static final Logger logger = LoggerFactory.getLogger(WalkService.class);
    
    // Earth's radius in meters (same as in WalkPointsService)
    private static final double EARTH_RADIUS_M = 6371000.0;
    
    private final PetRepository petRepository;
    private final WalkRepository walkRepository;
    private final WalkPointRepository walkPointRepository;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Autowired
    public WalkService(PetRepository petRepository, WalkRepository walkRepository, WalkPointRepository walkPointRepository) {
        this.petRepository = petRepository;
        this.walkRepository = walkRepository;
        this.walkPointRepository = walkPointRepository;
    }

    /**
     * Start a walk for a pet
     * @param petId the pet ID
     * @return StartWalkResponse with walk ID and start time
     * @throws PetNotFoundException if pet doesn't exist
     * @throws ActiveWalkExistsException if pet already has an active walk
     */
    @Transactional
    public StartWalkResponse startWalk(UUID petId) {
        // Get current user from security context
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = currentUser.getId();
        
        // Check if pet exists and belongs to the current user
        if (!petRepository.existsByIdAndUserId(petId, userId)) {
            throw new PetNotFoundException("Pet not found with ID: " + petId);
        }

        // Check if there's already an active walk for this pet
        if (walkRepository.existsActiveWalkByPetId(petId)) {
            throw new ActiveWalkExistsException("caminhada ativa já existe");
        }

        // Create new walk with server time
        LocalDateTime now = LocalDateTime.now();
        Walk walk = new Walk(petId, userId, now);
        Walk savedWalk = walkRepository.save(walk);

        // Return response with walk ID and ISO-8601 formatted start time
        String startedAt = now.format(ISO_FORMATTER);
        return new StartWalkResponse(savedWalk.getId(), startedAt);
    }

    /**
     * Stop a walk and compute consolidated metrics
     * @param walkId the walk ID
     * @return StopWalkResponse with consolidated metrics
     * @throws WalkNotFoundException if walk doesn't exist
     * @throws WalkFinishedException if walk is already finished
     */
    @Transactional
    public StopWalkResponse stopWalk(UUID walkId) {
        // Find the walk
        Walk walk = walkRepository.findById(walkId)
                .orElseThrow(() -> new WalkNotFoundException("Walk not found with ID: " + walkId));

        // Check if walk is already finished
        if (walk.getFinishedAt() != null) {
            throw new WalkFinishedException("walk already finished");
        }

        // Get all accepted points for the walk (ordered by timestamp)
        List<WalkPoint> points = walkPointRepository.findByWalkIdOrderByTimestamp(UUID.fromString(walkId.toString()));
        
        // Calculate total distance using Haversine formula
        double totalDistanceM = calculateTotalDistance(points);
        
        // Set finished time to current server time
        LocalDateTime finishedAt = LocalDateTime.now();
        
        // Calculate duration in seconds
        int duracaoS = (int) Duration.between(walk.getStartedAt(), finishedAt).getSeconds();
        
        // Calculate average speed in km/h
        double velMediaKmh = calculateAverageSpeed(totalDistanceM, duracaoS);
        
        // Update walk with metrics
        walk.setFinishedAt(finishedAt);
        walk.setDistanciaM(totalDistanceM);
        walk.setDuracaoS(duracaoS);
        walk.setVelMediaKmh(velMediaKmh);
        
        // Save the updated walk
        Walk savedWalk = walkRepository.save(walk);
        
        logger.info("Walk {} stopped: distance={}m, duration={}s, avg_speed={}km/h", 
                walkId, totalDistanceM, duracaoS, velMediaKmh);
        
        return new StopWalkResponse(
                savedWalk.getId(),
                savedWalk.getDistanciaM(),
                savedWalk.getDuracaoS(),
                savedWalk.getVelMediaKmh(),
                savedWalk.getStartedAt(),
                savedWalk.getFinishedAt()
        );
    }

    /**
     * Calculate total distance using Haversine formula between consecutive points
     * @param points list of walk points ordered by timestamp
     * @return total distance in meters
     */
    private double calculateTotalDistance(List<WalkPoint> points) {
        if (points.size() < 2) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        
        for (int i = 1; i < points.size(); i++) {
            WalkPoint prev = points.get(i - 1);
            WalkPoint curr = points.get(i);
            
            double distance = calculateHaversineDistance(
                    prev.getLatitude().doubleValue(),
                    prev.getLongitude().doubleValue(),
                    curr.getLatitude().doubleValue(),
                    curr.getLongitude().doubleValue()
            );
            
            totalDistance += distance;
        }
        
        return totalDistance;
    }

    /**
     * Calculate average speed in km/h
     * @param distanceM distance in meters
     * @param durationS duration in seconds
     * @return average speed in km/h rounded to 2 decimal places
     */
    private double calculateAverageSpeed(double distanceM, int durationS) {
        if (durationS == 0) {
            return 0.00;
        }
        
        // Convert to km/h: (distance_m / 1000) / (duration_s / 3600)
        double speedKmh = (distanceM / 1000.0) / (durationS / 3600.0);
        
        // Round to 2 decimal places (half-up)
        return BigDecimal.valueOf(speedKmh)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
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

    /**
     * List walks for a pet with pagination
     * @param petId the pet ID
     * @param page page number (zero-based)
     * @param size page size
     * @return WalksPageResponse with paginated walks
     * @throws PetNotFoundException if pet doesn't exist
     */
    @Transactional(readOnly = true)
    public WalksPageResponse listByPet(UUID petId, int page, int size) {
        // Get current user from security context
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = currentUser.getId();
        
        // Check if pet exists and belongs to the current user
        if (!petRepository.existsByIdAndUserId(petId, userId)) {
            throw new PetNotFoundException("Pet not found with ID: " + petId);
        }

        // Create pageable request
        Pageable pageable = PageRequest.of(page, size);
        
        // Get paginated walks
        Page<Walk> walksPage = walkRepository.findByPetIdOrderByStartedAtDesc(petId, pageable);
        
        // Convert to DTOs
        List<WalkListItem> walkItems = walksPage.getContent().stream()
                .map(walk -> new WalkListItem(
                        walk.getId(),
                        walk.getStartedAt(),
                        walk.getFinishedAt(),
                        walk.getDistanciaM(),
                        walk.getDuracaoS(),
                        walk.getVelMediaKmh()
                ))
                .collect(Collectors.toList());
        
        return new WalksPageResponse(
                walkItems,
                walksPage.getNumber(),
                walksPage.getSize(),
                walksPage.getTotalPages(),
                walksPage.getTotalElements()
        );
    }

    /**
     * Get GeoJSON representation of a walk's route
     * @param walkId the walk ID
     * @return WalkGeoJsonResponse with GeoJSON Feature containing LineString geometry
     * @throws WalkNotFoundException if walk doesn't exist
     */
    @Transactional(readOnly = true)
    public WalkGeoJsonResponse getGeoJson(UUID walkId) {
        // Check if walk exists
        if (!walkRepository.existsById(walkId)) {
            throw new WalkNotFoundException("Walk not found with ID: " + walkId);
        }

        // Get all accepted points for the walk (ordered by timestamp)
        List<WalkPoint> points = walkPointRepository.findByWalkIdOrderByTimestamp(walkId);
        
        // Convert points to GeoJSON coordinates [lon, lat] format
        List<List<Double>> coordinates = points.stream()
                .map(point -> List.of(
                        point.getLongitude().doubleValue(),
                        point.getLatitude().doubleValue()
                ))
                .collect(Collectors.toList());
        
        return new WalkGeoJsonResponse(walkId, coordinates);
    }

    /**
     * Get the active walk for a pet
     * @param petId the pet ID
     * @return StartWalkResponse with walk ID and start time, or null if no active walk
     * @throws PetNotFoundException if pet doesn't exist
     */
    public StartWalkResponse getActiveWalk(UUID petId) {
        // Get current user from security context
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = currentUser.getId();
        
        // Check if pet exists and belongs to the current user
        if (!petRepository.existsByIdAndUserId(petId, userId)) {
            throw new PetNotFoundException("Pet not found with ID: " + petId);
        }

        // Find active walk for this pet
        Optional<Walk> activeWalk = walkRepository.findActiveWalkByPetId(petId);
        
        if (activeWalk.isPresent()) {
            Walk walk = activeWalk.get();
            String startedAt = walk.getStartedAt().format(ISO_FORMATTER);
            return new StartWalkResponse(walk.getId(), startedAt);
        }
        
        return null; // No active walk found
    }
}
