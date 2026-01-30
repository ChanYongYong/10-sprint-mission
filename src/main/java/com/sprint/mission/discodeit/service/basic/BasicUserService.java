package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.common.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.DuplicateException;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;

    private final ReadStatusRepository readStatusRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse create(UserCreateRequest userCreateRequest, @Nullable BinaryContentCreateRequest profileRequest) {
        // 1. username 중복 체크
        if (userRepository.findByUsername(userCreateRequest.username()).isPresent()) {
            throw new DuplicateException("username", "중복되지 않은 값", userCreateRequest.username());
        }

        // 2. email 중복 체크
        if (userRepository.findByEmail(userCreateRequest.email()).isPresent()) {
            throw new DuplicateException("email", "중복되지 않은 값", userCreateRequest.email());
        }
        User user = new User(
                userCreateRequest.username(),
                userCreateRequest.email(),
                userCreateRequest.password(),
                userCreateRequest.nickname());

        // 4. 프로필 이미지 저장 (선택)
        Optional.ofNullable(profileRequest)
                .ifPresent(profile -> {
                    BinaryContent profileImage = new BinaryContent(
                            profile.fileName(),
                            profile.contentType(),
                            profile.data()
                    );
                    binaryContentRepository.save(profileImage);
                    user.updateProfileImage(profileImage.getId());
                });
        userRepository.save(user);

        // 6. UserStatus 생성
        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepository.save(userStatus);

        return userMapper.toDto(user, true);
    }

    @Override
    public UserResponse  findById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 유저", userId));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserResponse > findAll() {
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Override
    public UserResponse update(UUID userId, UserUpdateRequest request, @Nullable BinaryContentCreateRequest profileRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 유저", userId));

        Optional.ofNullable(request.username()).ifPresent(user::updateUsername);
        Optional.ofNullable(request.email()).ifPresent(user::updateEmail);
        Optional.ofNullable(request.nickname()).ifPresent(user::updateNickname);
        Optional.ofNullable(request.password()).ifPresent(user::updatePassword);

        if (profileRequest != null) {
            if (user.getProfileImageId() != null) {
                binaryContentRepository.deleteById(user.getProfileImageId());
            }
            BinaryContent newImage = new BinaryContent(
                    profileRequest.fileName(),
                    profileRequest.contentType(),
                    profileRequest.data()
            );
            binaryContentRepository.save(newImage);
            user.updateProfileImage(newImage.getId());
        }

        userRepository.save(user);
        return userMapper.toDto(user);
    }


    @Override
    public void hardDelete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 유저", userId));

        // BinaryContent(프로필) 삭제
        if (user.getProfileImageId() != null) {
            binaryContentRepository.deleteById(user.getProfileImageId());
        }

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

}
