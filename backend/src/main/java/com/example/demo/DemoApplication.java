package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        try {
            Files.createDirectories(Path.of("data")); // garante ./data antes do Flyway/DataSource
        } catch (Exception e) {
            System.err.println("[PatTrail] Falha ao criar pasta data/: " + e.getMessage());
        }
        SpringApplication.run(DemoApplication.class, args);
    }
}
