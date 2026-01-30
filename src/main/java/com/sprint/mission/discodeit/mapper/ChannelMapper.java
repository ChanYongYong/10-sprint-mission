package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;



    public ChannelResponse toDto(Channel channel){
        Instant lastMessageAt = channel.getMessageIds().stream()
                .map(messageRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Message::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

        List<UUID> participantIds = null;
        if(channel.getType() == ChannelType.PRIVATE){
            participantIds =readStatusRepository.findAllByChannelId(channel.getId())
                    .stream()
                    .map(ReadStatus::getUserId)
                    .collect(Collectors.toList());
        }
        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getType(),
                channel.getOwnerId(),
                participantIds,
                lastMessageAt
        );
    }

    public List<ChannelResponse> toDtoList(List<Channel> channels){
        return channels.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
