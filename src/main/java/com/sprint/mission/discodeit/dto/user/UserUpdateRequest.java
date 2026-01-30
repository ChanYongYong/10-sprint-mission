package com.sprint.mission.discodeit.dto.user;

public record UserUpdateRequest(
        String username,
        String email,
        String nickname,
        String password
) {}
