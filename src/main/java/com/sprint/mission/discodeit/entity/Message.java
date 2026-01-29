package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Message extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String content;
    private final UUID senderId;   // User sender → UUID senderId
    private final UUID channelId;
    private boolean editedAt;
    private boolean deletedAt;

    public Message(String content, UUID senderId,UUID channelId) {
        super();
        this.content = content;
        this.senderId = senderId;
        this.channelId = channelId;
        this.editedAt = false;
        this.deletedAt = false;
    }
    /*//연관관계 편의 메서드 - 메시지 삭제 시 채널, 유저에서도 삭제
    public void addToChannelAndUser(){
        if (this.channel != null && !this.channel.getMessages().contains(this)) {
            this.channel.getMessages().add(this);
        }
        if (this.sender != null && !this.sender.getMessages().contains(this)) {
            this.sender.getMessages().add(this);
        }
    }
    public void removeFromChannelAndUser() {
        if (this.channel != null) {
            this.channel.getMessages().remove(this);
        }
        if (this.sender != null) {
            this.sender.getMessages().remove(this);
        }
    }*/

    // Setters
    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = Instant.now();
    }

    public void updateEditedAt(boolean editedAt) {
        this.editedAt = editedAt;
        this.updatedAt = Instant.now();
    }

    public void updateDeletedAt(boolean deletedAt) {
        this.deletedAt = deletedAt;
        this.updatedAt = Instant.now();
    }

}
