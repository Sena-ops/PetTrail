package com.example.demo.service;

import com.example.demo.dto.WalkPointRequest;
import com.example.demo.dto.WalkPointsBatchResponse;
import com.example.demo.exception.WalkFinishedException;
import com.example.demo.exception.WalkNotFoundException;
import com.example.demo.model.Walk;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalkPointsServiceTest {

    @Mock
    private WalkRepository walkRepository;

    @Mock
    private WalkPointRepository walkPointRepository;

    @InjectMocks
    private WalkPointsService walkPointsService;

    private Walk activeWalk;
    private Walk finishedWalk;

    @BeforeEach
    void setUp() {
        activeWalk = new Walk(1L, LocalDateTime.now());
        activeWalk.setId(123L);

        finishedWalk = new Walk(1L, LocalDateTime.now().minusHours(1));
        finishedWalk.setId(456L);
        finishedWalk.setFinishedAt(LocalDateTime.now());
    }

    @Test
    void testIngestPoints_Success() {
        // Given
        Long walkId = 123L;
        List<WalkPointRequest> points = Arrays.asList(
                new WalkPointRequest(new BigDecimal("-23.5505"), new BigDecimal("-46.6333"), 
                        LocalDateTime.parse("2025-08-14T22:00:00")),
                new WalkPointRequest(new BigDecimal("-23.5510"), new BigDecimal("-46.6339"), 
                        LocalDateTime.parse("2025-08-14T22:00:10"))
        );

        when(walkRepository.findById(walkId)).thenReturn(Optional.of(activeWalk));
        when(walkPointRepository.saveAll(anyList())).thenReturn(Arrays.asList());

        // When
        WalkPointsBatchResponse response = walkPointsService.ingestPoints(walkId, points);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getReceived());
        assertEquals(2, response.getAccepted());
        assertEquals(0, response.getDiscarded());

        verify(walkRepository).findById(walkId);
        verify(walkPointRepository).saveAll(anyList());
    }

    @Test
    void testIngestPoints_WalkNotFound() {
        // Given
        Long walkId = 999L;
        List<WalkPointRequest> points = Arrays.asList(
                new WalkPointRequest(new BigDecimal("-23.5505"), new BigDecimal("-46.6333"), 
                        LocalDateTime.parse("2025-08-14T22:00:00"))
        );

        when(walkRepository.findById(walkId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(WalkNotFoundException.class, () -> {
            walkPointsService.ingestPoints(walkId, points);
        });

        verify(walkRepository).findById(walkId);
        verify(walkPointRepository, never()).saveAll(anyList());
    }

    @Test
    void testIngestPoints_WalkFinished() {
        // Given
        Long walkId = 456L;
        List<WalkPointRequest> points = Arrays.asList(
                new WalkPointRequest(new BigDecimal("-23.5505"), new BigDecimal("-46.6333"), 
                        LocalDateTime.parse("2025-08-14T22:00:00"))
        );

        when(walkRepository.findById(walkId)).thenReturn(Optional.of(finishedWalk));

        // When & Then
        assertThrows(WalkFinishedException.class, () -> {
            walkPointsService.ingestPoints(walkId, points);
        });

        verify(walkRepository).findById(walkId);
        verify(walkPointRepository, never()).saveAll(anyList());
    }

    @Test
    void testIngestPoints_OutlierDetection() {
        // Given - Two points with very high speed (should trigger outlier detection)
        Long walkId = 123L;
        List<WalkPointRequest> points = Arrays.asList(
                new WalkPointRequest(new BigDecimal("-23.5505"), new BigDecimal("-46.6333"), 
                        LocalDateTime.parse("2025-08-14T22:00:00")),
                new WalkPointRequest(new BigDecimal("-23.5600"), new BigDecimal("-46.6400"), 
                        LocalDateTime.parse("2025-08-14T22:00:01")) // Very far in 1 second
        );

        when(walkRepository.findById(walkId)).thenReturn(Optional.of(activeWalk));
        when(walkPointRepository.saveAll(anyList())).thenReturn(Arrays.asList());

        // When
        WalkPointsBatchResponse response = walkPointsService.ingestPoints(walkId, points);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getReceived());
        assertEquals(1, response.getAccepted()); // First point accepted
        assertEquals(1, response.getDiscarded()); // Second point discarded as outlier

        verify(walkRepository).findById(walkId);
        verify(walkPointRepository).saveAll(anyList());
    }

    @Test
    void testIngestPoints_DuplicateTimestamp() {
        // Given - Points with duplicate timestamps (non-increasing scenario)
        Long walkId = 123L;
        List<WalkPointRequest> points = Arrays.asList(
                new WalkPointRequest(new BigDecimal("-23.5505"), new BigDecimal("-46.6333"), 
                        LocalDateTime.parse("2025-08-14T22:00:00")),
                new WalkPointRequest(new BigDecimal("-23.5510"), new BigDecimal("-46.6339"), 
                        LocalDateTime.parse("2025-08-14T22:00:00")) // Same timestamp
        );

        when(walkRepository.findById(walkId)).thenReturn(Optional.of(activeWalk));
        when(walkPointRepository.saveAll(anyList())).thenReturn(Arrays.asList());

        // When
        WalkPointsBatchResponse response = walkPointsService.ingestPoints(walkId, points);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getReceived());
        assertEquals(1, response.getAccepted()); // First point accepted
        assertEquals(1, response.getDiscarded()); // Second point discarded due to non-increasing timestamp

        verify(walkRepository).findById(walkId);
        verify(walkPointRepository).saveAll(anyList());
    }
}
