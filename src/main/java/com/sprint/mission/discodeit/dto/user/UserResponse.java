package com.sprint.mission.discodeit.dto.user;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String username,
        String email,
        String nickname,
        UUID profileImageId,
        boolean online
) {}
