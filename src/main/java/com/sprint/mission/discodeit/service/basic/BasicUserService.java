package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Status;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;   // 추가
    private final MessageRepository messageRepository;   // 추가
    private final ReadStatusRepository readStatusRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public User create(String username, String email, String password, String nickname) {
        User user = new User(username, email, password, nickname);
        return userRepository.save(user);
    }

    @Override
    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 유저", userId));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User update(UUID userId, String username, String email, String nickname, String password) {
        User user = findById(userId);

        Optional.ofNullable(username).ifPresent(user::updateUsername);
        Optional.ofNullable(email).ifPresent(user::updateEmail);
        Optional.ofNullable(nickname).ifPresent(user::updateNickname);
        Optional.ofNullable(password).ifPresent(user::updatePassword);

        return userRepository.save(user);
    }

    @Override
    public void softDelete(UUID userId) {

    }


    @Override
    public void hardDelete(UUID userId) {
        User user = findById(userId);

        // 유저의 메시지 삭제
        messageRepository.findAllBySenderId(userId)
                .forEach(message -> messageRepository.deleteById(message.getId()));

        // 유저의 ReadStatus 삭제
        readStatusRepository.findAllByUserId(userId)
                .forEach(rs -> readStatusRepository.deleteById(rs.getId()));

        // 유저의 UserStatus 삭제
        userStatusRepository.findByUserId(userId)
                .ifPresent(status -> userStatusRepository.deleteById(status.getId()));

        userRepository.deleteById(userId);
    }
    @Override
    public List<Channel> findChannelByUser(UUID userId) {
        findById(userId);
        return readStatusRepository.findAllByUserId(userId).stream()
                .map(rs -> channelRepository.findById(rs.getChannelId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /*@Override
    public void hardDelete(UUID userId) {
        User user = findById(userId);

        // 채널에서 유저 삭제
        user.getChannels().forEach(channel -> {
            channel.getMembers().remove(user);
            channelRepository.save(channel);  //  추가
        });
        // 메시지 완전 삭제
        new ArrayList<>(user.getMessages()).forEach(message -> {
            Channel channel = message.getChannel();
            message.removeFromChannelAndUser();
            messageRepository.deleteById(message.getId());
            if (channel != null) {
                channelRepository.save(channel);  //  추가
            }
        });

        userRepository.deleteById(userId);
    }*/
/*
    @Override
    public List<Channel> findChannelByUser(UUID userId) {
        return findById(userId).getChannels();
    }*/
}
