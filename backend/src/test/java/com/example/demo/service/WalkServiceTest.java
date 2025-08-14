package com.example.demo.service;

import com.example.demo.dto.StopWalkResponse;
import com.example.demo.exception.WalkFinishedException;
import com.example.demo.exception.WalkNotFoundException;
import com.example.demo.model.Pet;
import com.example.demo.model.Walk;
import com.example.demo.model.WalkPoint;
import com.example.demo.repository.PetRepository;
import com.example.demo.repository.WalkPointRepository;
import com.example.demo.repository.WalkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalkServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private WalkRepository walkRepository;

    @Mock
    private WalkPointRepository walkPointRepository;

    @InjectMocks
    private WalkService walkService;

    private Pet testPet;
    private Walk activeWalk;
    private Walk finishedWalk;
    private List<WalkPoint> testPoints;

    @BeforeEach
    void setUp() {
        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("Buddy");
        testPet.setSpecies(com.example.demo.enums.Species.CACHORRO);

        LocalDateTime startTime = LocalDateTime.of(2025, 8, 13, 23, 15, 0);
        
        activeWalk = new Walk();
        activeWalk.setId(123L);
        activeWalk.setPetId(1L);
        activeWalk.setStartedAt(startTime);
        activeWalk.setFinishedAt(null);

        finishedWalk = new Walk();
        finishedWalk.setId(124L);
        finishedWalk.setPetId(1L);
        finishedWalk.setStartedAt(startTime);
        finishedWalk.setFinishedAt(LocalDateTime.of(2025, 8, 13, 23, 41, 0));

        // Create test points for distance calculation
        WalkPoint point1 = new WalkPoint();
        point1.setWalkId(123L);
        point1.setLatitude(new BigDecimal("-23.5505"));
        point1.setLongitude(new BigDecimal("-46.6333"));
        point1.setTimestamp(LocalDateTime.of(2025, 8, 13, 23, 15, 0));

        WalkPoint point2 = new WalkPoint();
        point2.setWalkId(123L);
        point2.setLatitude(new BigDecimal("-23.5510"));
        point2.setLongitude(new BigDecimal("-46.6339"));
        point2.setTimestamp(LocalDateTime.of(2025, 8, 13, 23, 20, 0));

        testPoints = Arrays.asList(point1, point2);
    }

    @Test
    void stopWalk_Success() {
        // Arrange
        when(walkRepository.findById(123L)).thenReturn(Optional.of(activeWalk));
        when(walkPointRepository.findByWalkIdOrderByTimestamp(123L)).thenReturn(testPoints);
        when(walkRepository.save(any(Walk.class))).thenReturn(activeWalk);

        // Act
        StopWalkResponse response = walkService.stopWalk(123L);

        // Assert
        assertNotNull(response);
        assertEquals(123L, response.getWalkId());
        assertTrue(response.getDistanciaM() > 0);
        assertTrue(response.getDuracaoS() > 0);
        assertTrue(response.getVelMediaKmh() >= 0);
        assertEquals(activeWalk.getStartedAt(), response.getStartedAt());
        assertNotNull(response.getFinishedAt());

        // Verify walk was saved with metrics
        verify(walkRepository).save(activeWalk);
        assertNotNull(activeWalk.getFinishedAt());
        assertNotNull(activeWalk.getDistanciaM());
        assertNotNull(activeWalk.getDuracaoS());
        assertNotNull(activeWalk.getVelMediaKmh());
    }

    @Test
    void stopWalk_WalkNotFound() {
        // Arrange
        when(walkRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WalkNotFoundException.class, () -> {
            walkService.stopWalk(999L);
        });

        verify(walkRepository, never()).save(any());
    }

    @Test
    void stopWalk_AlreadyFinished() {
        // Arrange
        when(walkRepository.findById(124L)).thenReturn(Optional.of(finishedWalk));

        // Act & Assert
        assertThrows(WalkFinishedException.class, () -> {
            walkService.stopWalk(124L);
        });

        verify(walkRepository, never()).save(any());
    }

    @Test
    void stopWalk_NoPoints() {
        // Arrange
        when(walkRepository.findById(123L)).thenReturn(Optional.of(activeWalk));
        when(walkPointRepository.findByWalkIdOrderByTimestamp(123L)).thenReturn(Arrays.asList());
        when(walkRepository.save(any(Walk.class))).thenReturn(activeWalk);

        // Act
        StopWalkResponse response = walkService.stopWalk(123L);

        // Assert
        assertNotNull(response);
        assertEquals(0.0, response.getDistanciaM());
        assertTrue(response.getDuracaoS() > 0);
        assertEquals(0.0, response.getVelMediaKmh());
    }

    @Test
    void stopWalk_SinglePoint() {
        // Arrange
        when(walkRepository.findById(123L)).thenReturn(Optional.of(activeWalk));
        when(walkPointRepository.findByWalkIdOrderByTimestamp(123L)).thenReturn(Arrays.asList(testPoints.get(0)));
        when(walkRepository.save(any(Walk.class))).thenReturn(activeWalk);

        // Act
        StopWalkResponse response = walkService.stopWalk(123L);

        // Assert
        assertNotNull(response);
        assertEquals(0.0, response.getDistanciaM());
        assertTrue(response.getDuracaoS() > 0);
        assertEquals(0.0, response.getVelMediaKmh());
    }
}
