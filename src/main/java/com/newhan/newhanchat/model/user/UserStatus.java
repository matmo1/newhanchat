package com.newhan.newhanchat.model.user;

import java.time.LocalDateTime;

public class UserStatus {
    private StatusType type = StatusType.OFFLINE;
    private LocalDateTime lasActive;

    protected StatusType getType() {
        return type;
    }

    protected LocalDateTime getLastActive() {
        return lasActive;
    }

    public void setStatus(StatusType s) {
        this.type = s;
    }

    public void setLastActive() {
        if (type == StatusType.OFFLINE || type == StatusType.AWAY) 
        lasActive = LocalDateTime.now();
        else lasActive = null;
    }
}
