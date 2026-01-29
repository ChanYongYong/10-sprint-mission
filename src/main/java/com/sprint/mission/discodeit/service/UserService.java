package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Status;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User create(String username, String email, String password, String nickname);

    User findById(UUID userId);
    List<User> findAll();

    User update(UUID userId, String username, String email, String nickname, String password);
//    User updateProfile(UUID id, String username, String email, String nickname);
//
//    void changePassword(UUID id, String newPassword);
//
//    void changeStatus(UUID id, Status status);

    void softDelete(UUID userId);

    void hardDelete(UUID userId);

    List<Channel> findChannelByUser(UUID userId);
}