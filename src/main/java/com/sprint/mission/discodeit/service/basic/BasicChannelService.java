package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.InvalidRequestException;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private final ReadStatusRepository readStatusRepository;  // ✅ 추가


    @Override
    public Channel create(String name, String description, ChannelType type, UUID ownerId) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("name", "null이 아니고 빈 값이 아님", name);
        }

        Channel channel = new Channel(name, description, type, ownerId);
        channelRepository.save(channel);

        ReadStatus ownerStatus = new ReadStatus(ownerId, channel.getId());
        readStatusRepository.save(ownerStatus);

        return channel;
    }

    @Override
    public Channel findById(UUID channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 채널", channelId));
    }

    @Override
    public List<Channel> findAll() {
        return channelRepository.findAll();
    }

    @Override
    public Channel update(UUID channelId, String name, String description, ChannelType type) {
        Channel channel = findById(channelId);

        Optional.ofNullable(name).ifPresent(channel::updateName);
        Optional.ofNullable(description).ifPresent(channel::updateDescription);
        Optional.ofNullable(type).ifPresent(channel::updateType);

        return channelRepository.save(channel);
    }

    @Override
    public void delete(UUID channelId) {
        Channel channel = findById(channelId);

        readStatusRepository.findAllByChannelId(channelId)
                .forEach(rs->readStatusRepository.deleteById(rs.getId()));

        channel.getMessageIds().forEach(messageId->{
            messageRepository.findById(messageId).flatMap(message -> userRepository.findById(message.getSenderId())).ifPresent(sender -> {
                sender.removeMessage(messageId);
                userRepository.save(sender);
            });
            messageRepository.deleteById(messageId);
        });
/*
        channel.getMessageIds().forEach(messageId -> {
            messageRepository.findById(messageId).ifPresent(message -> {
                userRepository.findById(message.getSenderId()).ifPresent(sender -> {
                    sender.removeMessage(messageId);
                    userRepository.save(sender);
                });
            });
            messageRepository.deleteById(messageId);
        });
*/

/*        new ArrayList<>(channel.getMessages()).forEach(message -> {  //차이점 보기
            User sender = message.getSender();  // 삭제 전에 sender 가져오기
            message.removeFromChannelAndUser();
            messageRepository.deleteById(message.getId());
            userRepository.save(sender);  // sender도 저장
        });*/

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

        /*        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 유저", userId));

        if (channel.getMembers().contains(user)) {
            throw new InvalidRequestException("userId", "채널에 없는 유저", userId);
        }

        channel.addMember(user); //여기도 잘 동작 되는건가? 확인해보기
        channelRepository.save(channel);
        userRepository.save(user);  //  추가*/
    }

    @Override
    public void removeMember(UUID channelId, UUID userId) {
        Channel channel = findById(channelId);
        if (channel.getOwnerId().equals(userId)) {
            throw new InvalidRequestException("userId", "채널 오너가 아님", userId);
        }

        // ✅ ReadStatus 삭제로 멤버 제거
        readStatusRepository.findAllByChannelId(channelId).stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .findFirst()
                .ifPresent(rs -> readStatusRepository.deleteById(rs.getId()));
   /*     User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 유저", userId));

        if (channel.getOwner().getId().equals(userId)) {
            throw new InvalidRequestException("userId", "채널 오너가 아님", userId);
        }

        channel.removeMember(user);
        channelRepository.save(channel);
        userRepository.save(user);*/
    }

    @Override
    public void transferOwnership(UUID channelId, UUID newOwnerId) {
        Channel channel = findById(channelId);
        // ✅ ReadStatus로 멤버 확인
        boolean isMember = readStatusRepository.findAllByChannelId(channelId).stream()
                .anyMatch(rs -> rs.getUserId().equals(newOwnerId));

        if (!isMember) {
            throw new InvalidRequestException("newOwnerId", "채널 멤버여야 함", newOwnerId);
        }

        channel.updateOwner(newOwnerId);  // ✅ UUID로 변경
        channelRepository.save(channel);
/*        User newOwner = userRepository.findById(newOwnerId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 유저", newOwnerId));

        if (!channel.getMembers().contains(newOwner)) {
            throw new InvalidRequestException("newOwnerId", "채널 멤버여야 함", newOwnerId);
        }

        channel.updateOwner(newOwner);
        channelRepository.save(channel);*/
    }

    @Override
    public List<User> findMembersByChannel(UUID channelId) {
        findById(channelId);  // 존재 확인

        return readStatusRepository.findAllByChannelId(channelId).stream()
                .map(rs -> userRepository.findById(rs.getUserId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

/*        Channel channel = findById(channelId);
        return channel.getMembers();*/
    }
}
