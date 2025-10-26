package com.example.pettrail.model;

import com.example.pettrail.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Schema(description = "Entity that represents a user")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Unique ID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    @Schema(description = "Email address of the user", example = "user@example.com", required = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(nullable = false)
    @Schema(description = "Hashed password of the user", required = true)
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Column(name = "first_name", nullable = false)
    @Schema(description = "First name of the user", example = "John", required = true, maxLength = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Column(name = "last_name", nullable = false)
    @Schema(description = "Last name of the user", example = "Doe", required = true, maxLength = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Role of the user", example = "USER", required = true, allowableValues = {"USER", "ADMIN"})
    private Role role = Role.USER;

    @Column(nullable = false)
    @Schema(description = "Whether the user account is enabled", example = "true")
    private Boolean enabled = true;

    @Column(name = "created_at")
    @Schema(description = "Creation timestamp of the user")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "Last update timestamp of the user")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public User() {}

    public User(String email, String password, String firstName, String lastName, Role role) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
