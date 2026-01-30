package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    Channel createPublic(PublicChannelCreateRequest request);

    Channel createPrivate(PrivateChannelCreateRequest request);


    ChannelResponse findById(UUID channelId);

    List<ChannelResponse > findAllByUserId(UUID userId);


    Channel update(UUID channelId, ChannelUpdateRequest request);

    void delete(UUID channelId);

    void addMember(UUID channelId, UUID userId);

    void removeMember(UUID channelId, UUID userId);

    void transferOwnership(UUID channelId, UUID newOwnerId);

    List<User> findMembersByChannel(UUID channelId);
}