package com.example.pettrail.repository;

import com.example.pettrail.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Find user by email address
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a user exists with the given email
     * @param email the email address to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);
}
