package com.example.pettrail.service;

import com.example.pettrail.dto.AuthResponse;
import com.example.pettrail.dto.LoginRequest;
import com.example.pettrail.dto.RegisterRequest;
import com.example.pettrail.enums.Role;
import com.example.pettrail.model.User;
import com.example.pettrail.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
            token,
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedUser.getRole()
        );
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            // Get user details
            User user = (User) authentication.getPrincipal();

            // Generate JWT token
            String token = jwtService.generateToken(user);

            return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    public User getCurrentUser(String token) {
        String email = jwtService.extractUsername(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}


