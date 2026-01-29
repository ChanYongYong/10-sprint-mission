package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private Instant lastActiveAt;

    public UserStatus (UUID userId){
        super();
        this.userId=userId;
        this.lastActiveAt=Instant.now();
    }

    public void updateLastActiveAt() {
        this.lastActiveAt = Instant.now();
    }

    public boolean isOnline(){
        Instant now = Instant.now();
        return now.isBefore(lastActiveAt.plusSeconds(300));
    }
}
