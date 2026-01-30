package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.InvalidRequestException;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final ChannelMapper channelMapper;


    @Override
    public Channel createPublic(PublicChannelCreateRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidRequestException("name", "null이 아니고 빈 값이 아님", request.name());
        }

        Channel channel = new Channel(request.name(), request.description(), ChannelType.PUBLIC, request.ownerId());
        channelRepository.save(channel);

        ReadStatus ownerStatus = new ReadStatus(request.ownerId(), channel.getId());
        readStatusRepository.save(ownerStatus);

        return channel;
    }


    @Override
    public Channel createPrivate(PrivateChannelCreateRequest request) {
        if(request.participantIds() == null || request.participantIds().isEmpty()){
            throw new InvalidRequestException("participantIds", "참여자가 최소 1명 이상 필요", null);
        }

        for (UUID userId : request.participantIds()) {
            if(!userRepository.existsById(userId)){
                throw new NotFoundException("id","유저가 존재해야 함",userId);
            }
        }

        Channel channel = new Channel(null,null, ChannelType.PRIVATE,null);
        channelRepository.save(channel);

        for (UUID userId : request.participantIds()) {
            ReadStatus readStatus = new ReadStatus(userId,channel.getId());
            readStatusRepository.save(readStatus);
        }
        return channel;
    }

    @Override
    public ChannelResponse findById(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 채널", channelId));
        return  channelMapper.toDto(channel);
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        // 유저가 참여한 채널 ID 목록 구하기
        List<UUID> userChannelIds = readStatusRepository.findAllByUserId(userId)
                .stream()
                .map(ReadStatus::getChannelId)
                .toList();
        //public 채널 전부, 유저가 속한 private 채널 반환
        return channelRepository.findAll().stream()
                .filter(channel ->{
                    if(channel.getType()==ChannelType.PUBLIC){
                        return true;
                    }  else{
                        return userChannelIds.contains(channel.getId());
                    }
                })
                .map(channelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Channel update(UUID channelId, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 채널", channelId));

        // PRIVATE 채널은 수정 불가
        if (channel.getType() == ChannelType.PRIVATE) {
            throw new InvalidRequestException("channelType", "PUBLIC 채널만 수정 가능", channel.getType());
        }

        Optional.ofNullable(request.name()).ifPresent(channel::updateName);
        Optional.ofNullable(request.description()).ifPresent(channel::updateDescription);

        return channelRepository.save(channel);
    }

    @Override
    public void delete(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 채널", channelId));
        // ReadStatus 삭제
        readStatusRepository.findAllByChannelId(channelId)
                .forEach(rs->readStatusRepository.deleteById(rs.getId()));
        //  Message 삭제
        channel.getMessageIds().forEach(messageId->{
            messageRepository.findById(messageId).flatMap(message -> userRepository.findById(message.getSenderId())).ifPresent(sender -> {
                sender.removeMessage(messageId);
                userRepository.save(sender);
            });
            messageRepository.deleteById(messageId);
        });

        // Channel 삭제
        channelRepository.deleteById(channelId);
    }

    @Override
    public void addMember(UUID channelId, UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("id", "존재하는 유저", userId);
        }

        //  ReadStatus로 멤버 확인
        boolean isMember = readStatusRepository.findAllByChannelId(channelId).stream()
                .anyMatch(rs -> rs.getUserId().equals(userId));

        if (isMember) {
            throw new InvalidRequestException("userId", "채널에 없는 유저", userId);
        }

        //  ReadStatus 생성으로 멤버 추가
        ReadStatus readStatus = new ReadStatus(userId, channelId);
        readStatusRepository.save(readStatus);

    }

    @Override
    public void removeMember(UUID channelId, UUID userId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 채널", channelId));
        if (channel.getOwnerId().equals(userId)) {
            throw new InvalidRequestException("userId", "채널 오너가 아님", userId);
        }

        //  ReadStatus 삭제로 멤버 제거
        readStatusRepository.findAllByChannelId(channelId).stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .findFirst()
                .ifPresent(rs -> readStatusRepository.deleteById(rs.getId()));

    }

    @Override
    public void transferOwnership(UUID channelId, UUID newOwnerId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 채널", channelId));
        //  ReadStatus로 멤버 확인
        boolean isMember = readStatusRepository.findAllByChannelId(channelId).stream()
                .anyMatch(rs -> rs.getUserId().equals(newOwnerId));

        if (!isMember) {
            throw new InvalidRequestException("newOwnerId", "채널 멤버여야 함", newOwnerId);
        }

        channel.updateOwner(newOwnerId);  //  UUID로 변경
        channelRepository.save(channel);

    }

    @Override
    public List<User> findMembersByChannel(UUID channelId) {
        findById(channelId);  // 존재 확인

        return readStatusRepository.findAllByChannelId(channelId).stream()
                .map(rs -> userRepository.findById(rs.getUserId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());


    }
}
