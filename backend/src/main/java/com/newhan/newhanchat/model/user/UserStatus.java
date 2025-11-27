package com.newhan.newhanchat.model.user;

import java.time.LocalDateTime;

public class UserStatus {
    private StatusType type = StatusType.OFFLINE;
    private LocalDateTime lasActive;

    public StatusType getType() {
        return type;
    }

    public LocalDateTime getLastActive() {
        return lasActive;
    }

    public void setStatus(StatusType s) {
        this.type = s;
        if (type == StatusType.OFFLINE || type == StatusType.AWAY) 
        lasActive = LocalDateTime.now();
        else lasActive = null;
    }
}
