package com.example.demo.repository;

import com.example.demo.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    
    List<Pet> findByEspecie(String especie);
    
    List<Pet> findByRacaContainingIgnoreCase(String raca);
    
    List<Pet> findByNomeContainingIgnoreCase(String nome);
}
