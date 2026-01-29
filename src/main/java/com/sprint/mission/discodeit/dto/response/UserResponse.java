package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.User;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String nickname,
        UUID profileImageId,
        boolean online
) {}
