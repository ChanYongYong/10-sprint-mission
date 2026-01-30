package com.sprint.mission.discodeit.dto.common;

public record BinaryContentCreateRequest(
        String fileName,
        String contentType,
        byte[] data
) {}
