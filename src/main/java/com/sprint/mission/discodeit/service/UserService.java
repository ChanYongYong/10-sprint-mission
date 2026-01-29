package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Status;
import com.sprint.mission.discodeit.entity.User;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse  create(UserCreateRequest request, BinaryContentCreateRequest profileRequest);

    UserResponse findById(UUID userId);
    List<UserResponse> findAll();

    UserResponse update(UserUpdateRequest request, @Nullable BinaryContentCreateRequest profileRequest);
//    User updateProfile(UUID id, String username, String email, String nickname);
//
//    void changePassword(UUID id, String newPassword);
//
//    void changeStatus(UUID id, Status status);


    void hardDelete(UUID userId);

    List<Channel> findChannelByUser(UUID userId);
}