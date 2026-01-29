package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import static java.time.LocalTime.now;

@Getter
public class ReadStatus  extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Instant lastReadAt;
    private final UUID userId;
    private final UUID channelId;

    public ReadStatus(UUID userId, UUID channelId){
        super();
        this.userId=userId;
        this.channelId=channelId;
        this.lastReadAt=Instant.now();
    }
    public Instant updateLastReadAt(){
        return this.lastReadAt = Instant.now();
    }

}
