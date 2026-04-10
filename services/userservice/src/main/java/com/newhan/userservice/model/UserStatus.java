package com.newhan.userservice.model;

import java.time.LocalDateTime;

import jakarta.persistence.Embeddable;


@Embeddable
public class UserStatus {
    private StatusType type = StatusType.OFFLINE;
    
    // Stores manual selection (e.g. user sets themselves to BUSY)
    private StatusType preferredStatus = StatusType.ONLINE;
    
    private LocalDateTime lastSeen;

    public UserStatus() {}

    public UserStatus(StatusType type, StatusType preferredStatus) {
        this.type = type;
        this.preferredStatus = preferredStatus;
    }

    // --- Custom Logic Methods ---
    
    // Helper to decide what to show the world
    public StatusType getDisplayStatus() {
        if (type == StatusType.OFFLINE) return StatusType.OFFLINE;
        return preferredStatus; // If connected, show what the user prefers (Online or Busy)
    }

    public void setType(StatusType type) {
        this.type = type;
        if (type == StatusType.ONLINE) {
            this.lastSeen = LocalDateTime.now();
        }
    }

    // Getters & Setters
    public StatusType getType() { return type; }
    public StatusType getPreferredStatus() { return preferredStatus; }
    public void setPreferredStatus(StatusType preferredStatus) { this.preferredStatus = preferredStatus; }
    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
}