package com.sprint.mission.discodeit.entity;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class User extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String email;
    private String password;
    private String nickname;
    private final List<UUID> messageIds;  // 👈 추가
//    private Status status;

    public User (String username, String email, String password, String nickname) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.messageIds = new ArrayList<>(); //참고로 타입 명시 안해도 됨
//        this.status = Status.ACTIVE;
    }


    // Setters
    public void updateUsername(String username) {
        this.username = username;
        this.updatedAt = Instant.now();
    }

    public void updateEmail(String email) {
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.updatedAt = Instant.now();
    }

    public void updatePassword(String password) {
        this.password = password;
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

}
