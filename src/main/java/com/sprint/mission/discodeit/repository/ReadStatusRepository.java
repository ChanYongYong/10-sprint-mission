package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {
    ReadStatus save(ReadStatus readStatus);
    Optional<ReadStatus> findById(UUID readStatusId);
    List<ReadStatus> findAllByUserId(UUID userId);      // 유저의 모든 읽음 상태
    List<ReadStatus> findAllByChannelId(UUID channelId); // 채널의 모든 읽음 상태
    List<ReadStatus> findAll();
    void deleteById(UUID readStatusId);
    boolean existsById(UUID readStatusId);
}
