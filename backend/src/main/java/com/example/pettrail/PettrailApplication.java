package com.example.pettrail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class PettrailApplication {

    public static void main(String[] args) {
        try {
            Files.createDirectories(Path.of("data")); // garante ./data antes do Flyway/DataSource
        } catch (Exception e) {
            System.err.println("[PetTrail] Falha ao criar pasta data/: " + e.getMessage());
        }
        SpringApplication.run(PettrailApplication.class, args);
    }
}
