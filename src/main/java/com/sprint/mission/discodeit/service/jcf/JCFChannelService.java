package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.InvalidRequestException;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFChannelService implements ChannelService {

    private final Map<UUID,Channel> data;
    private final UserService userService;
    private MessageService messageService;


    public JCFChannelService(UserService userService){
        data = new HashMap<>();
        this.userService = userService;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Channel create(String name, String description,ChannelType type, UUID ownerId) {
        // 입력값 검증 추가
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("name", "null이 아니고 빈 값이 아님", name);
        }
        User owner =userService.findById(ownerId);
        Channel channel = new Channel(name, description, type, owner);

        data.put(channel.getId(),channel);

        owner.getChannels().add(channel);

        return channel;
    }

    @Override
    public Channel findById(UUID channelId) {
        return Optional.ofNullable(data.get(channelId))
                .orElseThrow(()->new NotFoundException("id", "존재하는 채널", channelId));
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Channel update(UUID channelId, String name, String description, ChannelType type) {
        Channel channel = findById(channelId);

        // null이 아닌 값만 업데이트
        Optional.ofNullable(name).ifPresent(channel::updateName);
        Optional.ofNullable(description).ifPresent(channel::updateDescription);
        Optional.ofNullable(type).ifPresent(channel::updateType);

        return channel;
    }

    /*@Override Optional로 개선
    public Channel update(UUID channelId, String name, String description, ChannelType type) {
        Channel channel = findById(channelId);
        channel.setName(name);
        channel.setDescription(description);
        channel.setType(type);
        return channel;
    }*/

    @Override
    public void delete(UUID channelId) {
        Channel  channel = findById(channelId);
//      for(User member: channel.getMembers()){ member.getChannels().remove(channel);}    코드 개선
        channel.getMembers().forEach(member->member.getChannels().remove(channel));

        // 2. 채널의 메시지 완전 삭제
        new ArrayList<>(channel.getMessages()).forEach(message -> //추가
                messageService.hardDelete(message.getId())
        );

        data.remove(channelId);
    }

    @Override
    public void addMember(UUID channelId, UUID userId) {
        Channel channel = findById(channelId);
        User user = userService.findById(userId);

        // 중복 멤버 검증 추가
        if (channel.getMembers().contains(user)) {
            throw new InvalidRequestException("userId", "채널에 없는 유저", userId);
        }
//        channel.getMembers().add(user);
//        user.getChannels().add(channel);
        channel.addMember(user);  // 편의 메서드 사용

    }

    @Override
    public void removeMember(UUID channelId, UUID userId) {
        Channel channel = findById(channelId);
        User user = userService.findById(userId);

        // 오너는 나갈 수 없음 검증 추가
        if (channel.getOwner().getId().equals(userId)) {
            throw new InvalidRequestException("userId", "채널 오너가 아님", userId);
        }
//        channel.getMembers().remove(user);
//        user.getChannels().remove(channel);
        channel.removeMember(user);// 편의 메서드 사용
    }


    @Override
    public void transferOwnership(UUID channelId, UUID newOwnerId) {
        Channel channel = findById(channelId);
        User newOwner = userService.findById(newOwnerId);

        // 채널 멤버 검증
        if (!channel.getMembers().contains(newOwner)) {
            throw new InvalidRequestException("newOwnerId", "채널 멤버여야 함", newOwnerId);
        }
        channel.updateOwner(newOwner);
    }

    @Override
    public List<User> findMembersByChannel(UUID channelId) {
        Channel channel = findById(channelId);
        return channel.getMembers();
    }
}
