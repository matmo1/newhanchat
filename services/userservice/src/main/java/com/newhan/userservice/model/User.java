package com.newhan.userservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password; // Hashed
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime dateOfBirth;

    // Profile Fields
    private String bio;
    private String profilePictureUrl;
    
    @Embedded // Embeds UserStatus fields into the user table
    private UserStatus status = new UserStatus();

    // --- Standard Getters and Setters (No Lombok) ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    // Delegation methods to fix UserService and UserController mismatches
    public LocalDateTime getLastSeen() {
        return status != null ? status.getLastSeen() : null;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        if (this.status == null) this.status = new UserStatus();
        this.status.setLastSeen(lastSeen);
    }
}