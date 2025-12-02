package com.newhan.newhanchat.model.user;

import java.time.LocalDateTime;

public class UserStatus {
    private StatusType type = StatusType.OFFLINE;
    
    // New field to remember manual selection (e.g. BUSY)
    private StatusType preferredStatus = StatusType.ONLINE; 
    
    private LocalDateTime lasActive;

    public StatusType getType() {
        return type;
    }

    public void setType(StatusType type) {
        this.type = type;
    }

    public StatusType getPreferredStatus() {
        return preferredStatus;
    }

    public void setPreferredStatus(StatusType preferredStatus) {
        this.preferredStatus = preferredStatus;
    }

    public LocalDateTime getLastActive() {
        return lasActive;
    }

    // Updated logic: Set both if manual, or handle offline
    public void setStatus(StatusType s) {
        this.type = s;
        // If user manually sets status, remember it as their preference
        if (s != StatusType.OFFLINE) {
            this.preferredStatus = s;
        }
        
        if (s == StatusType.OFFLINE) {
            lasActive = LocalDateTime.now();
        } else {
            lasActive = null;
        }
    }
    
    // Helper for Auto-Online
    public void setOnline() {
        // Restore the preferred status (e.g. if I was BUSY, stay BUSY)
        // If I was OFFLINE, default to ONLINE
        if (preferredStatus == null || preferredStatus == StatusType.OFFLINE) {
            this.type = StatusType.ONLINE;
        } else {
            this.type = preferredStatus;
        }
        this.lasActive = null;
    }

    // Helper for Auto-Offline
    public void setOffline() {
        this.type = StatusType.OFFLINE;
        this.lasActive = LocalDateTime.now();
    }
}