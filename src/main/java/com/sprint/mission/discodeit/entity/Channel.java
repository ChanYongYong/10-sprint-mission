package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private ChannelType type;
    private final List<UUID> messageIds;
    private UUID ownerId;


//    private List<User> members;
//    private List<Message> messages;
//    private User owner;

    public Channel(String name, String description, ChannelType type,  UUID ownerId) {
        super();
        this.name = name;
        this.description = description;
        this.type = type;
//        this.memberIds = new ArrayList<>();
//        this.memberIds.add(ownerId);
        this.messageIds = new ArrayList<>();  //messages;
        this.ownerId = ownerId;
    }

/*
    // 연관관계 편의 메서드 유저 추가 삭제
    public void addMember(User user) {
        if (!this.memberIds.contains(user)) {
            this.memberIds.add(user);
            user.getChannels().add(this);
        }
    }
    // 연관관계 편의 메서드 - 유저 제거
    public void removeMember(User user) {
        this.members.remove(user);
        user.getChannels().remove(this);
    }        이건 객체 참조할 때만 쓰는건가? 그럼 지금 이 역할은 어디서 정의해야 하나?

        public boolean hasMember(UUID userId) {
        return this.memberIds.contains(userId);
    }
*/
/*// Channel.java에 추가          이걸 추가하라는데, 이건 엔티티가 아니라 서비스에서 적어야 하는거 아닐까??
public void addMember(UUID userId) {
    if (!this.memberIds.contains(userId)) {
        this.memberIds.add(userId);
        this.updatedAt = Instant.now();
    }
}

    public void removeMember(UUID userId) {
        this.memberIds.remove(userId);
        this.updatedAt = Instant.now();
    }*/
    // Setters
    public void updateName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void updateDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void updateType(ChannelType type) {
        this.type = type;
        this.updatedAt = Instant.now();
    }
    // User.java에 추가
    public void addMessage(UUID messageId) {
        this.messageIds.add(messageId);
        this.updatedAt = Instant.now();
    }

    public void removeMessage(UUID messageId) {
        this.messageIds.remove(messageId);
        this.updatedAt = Instant.now();
    }


    public void updateOwner(UUID ownerId) {
        this.ownerId = ownerId;
        this.updatedAt = Instant.now();
    }

}
