package com.example.demo.service;

import com.example.demo.dto.StartWalkResponse;
import com.example.demo.exception.PetNotFoundException;
import com.example.demo.exception.ActiveWalkExistsException;
import com.example.demo.model.Pet;
import com.example.demo.model.Walk;
import com.example.demo.repository.PetRepository;
import com.example.demo.repository.WalkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class WalkService {

    private final PetRepository petRepository;
    private final WalkRepository walkRepository;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Autowired
    public WalkService(PetRepository petRepository, WalkRepository walkRepository) {
        this.petRepository = petRepository;
        this.walkRepository = walkRepository;
    }

    /**
     * Start a walk for a pet
     * @param petId the pet ID
     * @return StartWalkResponse with walk ID and start time
     * @throws PetNotFoundException if pet doesn't exist
     * @throws ActiveWalkExistsException if pet already has an active walk
     */
    @Transactional
    public StartWalkResponse startWalk(Long petId) {
        // Check if pet exists
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found with ID: " + petId));

        // Check if there's already an active walk for this pet
        if (walkRepository.existsActiveWalkByPetId(petId)) {
            throw new ActiveWalkExistsException("caminhada ativa já existe");
        }

        // Create new walk with server time
        LocalDateTime now = LocalDateTime.now();
        Walk walk = new Walk(petId, now);
        Walk savedWalk = walkRepository.save(walk);

        // Return response with walk ID and ISO-8601 formatted start time
        String startedAt = now.format(ISO_FORMATTER);
        return new StartWalkResponse(savedWalk.getId(), startedAt);
    }
}
