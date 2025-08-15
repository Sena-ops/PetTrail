package com.example.pettrail.service;

import com.example.pettrail.dto.StopWalkResponse;
import com.example.pettrail.dto.WalksPageResponse;
import com.example.pettrail.dto.WalkListItem;
import com.example.pettrail.exception.WalkFinishedException;
import com.example.pettrail.exception.WalkNotFoundException;
import com.example.pettrail.exception.PetNotFoundException;
import com.example.pettrail.model.Pet;
import com.example.pettrail.model.Walk;
import com.example.pettrail.model.WalkPoint;
import com.example.pettrail.repository.PetRepository;
import com.example.pettrail.repository.WalkPointRepository;
import com.example.pettrail.repository.WalkRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
        testPet.setSpecies(com.example.pettrail.enums.Species.CACHORRO);

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

    @Test
    void listByPet_Success() {
        // Arrange
        Long petId = 1L;
        int page = 0;
        int size = 10;
        
        when(petRepository.existsById(petId)).thenReturn(true);
        
        List<Walk> walks = Arrays.asList(finishedWalk, activeWalk);
        Page<Walk> walksPage = new PageImpl<>(walks, PageRequest.of(page, size), 2);
        when(walkRepository.findByPetIdOrderByStartedAtDesc(petId, PageRequest.of(page, size)))
                .thenReturn(walksPage);

        // Act
        WalksPageResponse response = walkService.listByPet(petId, page, size);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalPages());
        assertEquals(2L, response.getTotalElements());
        
        // Verify content is ordered by startedAt DESC
        WalkListItem firstWalk = response.getContent().get(0);
        assertEquals(finishedWalk.getId(), firstWalk.getId());
        assertEquals(finishedWalk.getStartedAt(), firstWalk.getStartedAt());
        assertEquals(finishedWalk.getFinishedAt(), firstWalk.getFinishedAt());
        assertEquals(finishedWalk.getDistanciaM(), firstWalk.getDistanciaM());
        assertEquals(finishedWalk.getDuracaoS(), firstWalk.getDuracaoS());
        assertEquals(finishedWalk.getVelMediaKmh(), firstWalk.getVelMediaKmh());
    }

    @Test
    void listByPet_PetNotFound() {
        // Arrange
        Long petId = 999L;
        int page = 0;
        int size = 10;
        
        when(petRepository.existsById(petId)).thenReturn(false);

        // Act & Assert
        assertThrows(PetNotFoundException.class, () -> {
            walkService.listByPet(petId, page, size);
        });

        verify(walkRepository, never()).findByPetIdOrderByStartedAtDesc(any(), any());
    }

    @Test
    void listByPet_EmptyResult() {
        // Arrange
        Long petId = 1L;
        int page = 0;
        int size = 10;
        
        when(petRepository.existsById(petId)).thenReturn(true);
        
        Page<Walk> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(page, size), 0);
        when(walkRepository.findByPetIdOrderByStartedAtDesc(petId, PageRequest.of(page, size)))
                .thenReturn(emptyPage);

        // Act
        WalksPageResponse response = walkService.listByPet(petId, page, size);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(0, response.getTotalPages());
        assertEquals(0L, response.getTotalElements());
    }

    @Test
    void listByPet_Pagination() {
        // Arrange
        Long petId = 1L;
        int page = 1;
        int size = 5;
        
        when(petRepository.existsById(petId)).thenReturn(true);
        
        List<Walk> walks = Arrays.asList(finishedWalk);
        Page<Walk> walksPage = new PageImpl<>(walks, PageRequest.of(page, size), 6);
        when(walkRepository.findByPetIdOrderByStartedAtDesc(petId, PageRequest.of(page, size)))
                .thenReturn(walksPage);

        // Act
        WalksPageResponse response = walkService.listByPet(petId, page, size);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getPage());
        assertEquals(5, response.getSize());
        assertEquals(2, response.getTotalPages());
        assertEquals(6L, response.getTotalElements());
    }
}
