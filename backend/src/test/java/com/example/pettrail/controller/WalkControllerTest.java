package com.example.pettrail.controller;

import com.example.pettrail.dto.WalksPageResponse;
import com.example.pettrail.dto.WalkListItem;
import com.example.pettrail.dto.WalkGeoJsonResponse;
import com.example.pettrail.exception.PetNotFoundException;
import com.example.pettrail.exception.PaginationValidationException;
import com.example.pettrail.exception.WalkNotFoundException;
import com.example.pettrail.service.WalkService;
import com.example.pettrail.service.WalkPointsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WalkControllerTest {

    @Mock
    private WalkService walkService;

    @Mock
    private WalkPointsService walkPointsService;

    @InjectMocks
    private WalkController walkController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(walkController)
                .setControllerAdvice(new com.example.pettrail.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listWalksByPet_Success() throws Exception {
        // Arrange
        Long petId = 42L;
        LocalDateTime startedAt = LocalDateTime.of(2025, 8, 13, 23, 15, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2025, 8, 13, 23, 41, 0);
        
        WalkListItem walkItem = new WalkListItem(101L, startedAt, finishedAt, 2450.7, 1560, 5.65);
        WalksPageResponse expectedResponse = new WalksPageResponse(
                Arrays.asList(walkItem), 0, 10, 3, 21L
        );
        
        when(walkService.listByPet(petId, 0, 10)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/walks")
                        .param("petId", "42")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(101))
                .andExpect(jsonPath("$.content[0].startedAt").exists())
                .andExpect(jsonPath("$.content[0].finishedAt").exists())
                .andExpect(jsonPath("$.content[0].distanciaM").value(2450.7))
                .andExpect(jsonPath("$.content[0].duracaoS").value(1560))
                .andExpect(jsonPath("$.content[0].velMediaKmh").value(5.65))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.totalElements").value(21));
    }

    @Test
    void listWalksByPet_WithDefaults() throws Exception {
        // Arrange
        Long petId = 42L;
        WalksPageResponse expectedResponse = new WalksPageResponse(
                Arrays.asList(), 0, 10, 0, 0L
        );
        
        when(walkService.listByPet(petId, 0, 10)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/walks")
                        .param("petId", "42")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void listWalksByPet_PetNotFound() throws Exception {
        // Arrange
        Long petId = 999L;
        when(walkService.listByPet(petId, 0, 10))
                .thenThrow(new PetNotFoundException("Pet not found with ID: " + petId));

        // Act & Assert
        mockMvc.perform(get("/api/walks")
                        .param("petId", "999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("pet not found"))
                .andExpect(jsonPath("$.details[0].field").value("petId"))
                .andExpect(jsonPath("$.details[0].issue").value("unknown"));
    }

    @Test
    void listWalksByPet_InvalidPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/walks")
                        .param("petId", "42")
                        .param("page", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid pagination parameters."))
                .andExpect(jsonPath("$.details[0].field").value("page"))
                .andExpect(jsonPath("$.details[0].issue").value("must be >= 0"));
    }

    @Test
    void listWalksByPet_InvalidSizeTooSmall() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/walks")
                        .param("petId", "42")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid pagination parameters."))
                .andExpect(jsonPath("$.details[0].field").value("size"))
                .andExpect(jsonPath("$.details[0].issue").value("must be between 1 and 100"));
    }

    @Test
    void listWalksByPet_InvalidSizeTooLarge() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/walks")
                        .param("petId", "42")
                        .param("size", "101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid pagination parameters."))
                .andExpect(jsonPath("$.details[0].field").value("size"))
                .andExpect(jsonPath("$.details[0].issue").value("must be between 1 and 100"));
    }

    @Test
    void listWalksByPet_MissingPetId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/walks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Missing required parameter."))
                .andExpect(jsonPath("$.details[0].field").value("petId"))
                .andExpect(jsonPath("$.details[0].issue").value("required"));
    }

    @Test
    void listWalksByPet_InvalidPetId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/walks")
                        .param("petId", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid query parameter."))
                .andExpect(jsonPath("$.details[0].field").value("petId"))
                .andExpect(jsonPath("$.details[0].issue").value("required numeric id"));
    }

    @Test
    void getWalkGeoJson_Success() throws Exception {
        // Arrange
        Long walkId = 123L;
        WalkGeoJsonResponse expectedResponse = new WalkGeoJsonResponse(
                walkId, 
                Arrays.asList(
                        Arrays.asList(-46.6333, -23.5505),
                        Arrays.asList(-46.6339, -23.5510)
                )
        );
        
        when(walkService.getGeoJson(walkId)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/walks/123/geojson")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("Feature"))
                .andExpect(jsonPath("$.geometry.type").value("LineString"))
                .andExpect(jsonPath("$.geometry.coordinates[0][0]").value(-46.6333))
                .andExpect(jsonPath("$.geometry.coordinates[0][1]").value(-23.5505))
                .andExpect(jsonPath("$.geometry.coordinates[1][0]").value(-46.6339))
                .andExpect(jsonPath("$.geometry.coordinates[1][1]").value(-23.5510))
                .andExpect(jsonPath("$.properties.walkId").value(123));
    }

    @Test
    void getWalkGeoJson_EmptyRoute() throws Exception {
        // Arrange
        Long walkId = 123L;
        WalkGeoJsonResponse expectedResponse = new WalkGeoJsonResponse(walkId, Arrays.asList());
        
        when(walkService.getGeoJson(walkId)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/walks/123/geojson")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("Feature"))
                .andExpect(jsonPath("$.geometry.type").value("LineString"))
                .andExpect(jsonPath("$.geometry.coordinates").isEmpty())
                .andExpect(jsonPath("$.properties.walkId").value(123));
    }

    @Test
    void getWalkGeoJson_WalkNotFound() throws Exception {
        // Arrange
        Long walkId = 999L;
        when(walkService.getGeoJson(walkId))
                .thenThrow(new WalkNotFoundException("Walk not found with ID: " + walkId));

        // Act & Assert
        mockMvc.perform(get("/api/walks/999/geojson")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("walk not found"))
                .andExpect(jsonPath("$.details[0].field").value("id"))
                .andExpect(jsonPath("$.details[0].issue").value("unknown"));
    }

    @Test
    void getWalkGeoJson_InvalidWalkId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/walks/invalid/geojson")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid query parameter."))
                .andExpect(jsonPath("$.details[0].field").value("id"))
                .andExpect(jsonPath("$.details[0].issue").value("required numeric id"));
    }
}
