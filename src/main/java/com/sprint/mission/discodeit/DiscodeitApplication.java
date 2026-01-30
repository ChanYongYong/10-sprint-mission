package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.common.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

        UserService userService = context.getBean(UserService.class);
        ChannelService channelService = context.getBean(ChannelService.class);
        MessageService messageService = context.getBean(MessageService.class);
        AuthService authService = context.getBean(AuthService.class);

        // ==================== 1. 등록 테스트 ====================
        System.out.println("\n[ 1. 등록 테스트 ]");
        System.out.println("----------------------------------------");

        // 유저 등록
        UserCreateRequest userARequest = new UserCreateRequest("a", "a@test.com", "pw", "유저A");
        UserCreateRequest userBRequest = new UserCreateRequest("b", "b@test.com", "pw", "유저B");
        UserResponse userA = userService.create(userARequest, null);
        UserResponse userB = userService.create(userBRequest, null);
        UUID aId = userA.userId();
        UUID bId = userB.userId();
        System.out.println(">> 유저 등록 완료: " + userService.findAll().stream()
                .map(UserResponse::username).toList());
        System.out.println(">> 유저A 온라인 상태: " + userA.online());

        // 프로필 이미지와 함께 유저 등록
        UserCreateRequest userCRequest = new UserCreateRequest("c", "c@test.com", "pw", "유저C");
        BinaryContentCreateRequest profileRequest = new BinaryContentCreateRequest(
                "profile.png",
                "image/png",
                new byte[]{1, 2, 3}
        );
        UserResponse userC = userService.create(userCRequest, profileRequest);
        System.out.println(">> 프로필 이미지 있는 유저 등록: " + userC.username());
        System.out.println(">> 프로필 이미지 ID: " + userC.profileImageId());

        // PUBLIC 채널 등록
        PublicChannelCreateRequest publicChannelRequest = new PublicChannelCreateRequest(
                "테스트채널",
                "설명",
                aId
        );
        Channel channel = channelService.createPublic(publicChannelRequest);
        UUID channelId = channel.getId();
        channelService.addMember(channelId, bId);
        System.out.println(">> PUBLIC 채널 등록 완료: " + channelService.findAllByUserId(aId).stream()
                .map(ChannelResponse::name).toList());

        // PRIVATE 채널 등록
        PrivateChannelCreateRequest privateChannelRequest = new PrivateChannelCreateRequest(
                List.of(aId, bId)
        );
        Channel privateChannel = channelService.createPrivate(privateChannelRequest);
        System.out.println(">> PRIVATE 채널 등록 완료, ID: " + privateChannel.getId());
        System.out.println(">> PRIVATE 채널 이름(null이어야 함): " + privateChannel.getName());

        // 메시지 등록
        Message msg1 = messageService.create("첫번째 메시지", aId, channelId);
        Message msg2 = messageService.create("두번째 메시지", bId, channelId);
        System.out.println(">> 메시지 등록 완료: " + messageService.findAll().stream()
                .map(Message::getContent).toList());

        // ==================== 2. 조회 테스트 ====================
        System.out.println("\n[ 2. 조회 테스트 ]");
        System.out.println("----------------------------------------");

        // 유저 단건 조회
        UserResponse foundUser = userService.findById(aId);
        System.out.println(">> 유저 단건 조회: " + foundUser.username());
        System.out.println(">> 유저 온라인 상태: " + foundUser.online());
        System.out.println(">> 유저 이메일: " + foundUser.email());

        // 유저 다건 조회
        System.out.println(">> 유저 다건 조회: " + userService.findAll().size() + "명");

        // 채널 단건 조회 (ChannelResponse 반환)
        ChannelResponse foundChannel = channelService.findById(channelId);
        System.out.println(">> 채널 단건 조회: " + foundChannel.name());
        System.out.println(">> 채널 최근 메시지 시간: " + foundChannel.lastMessageAt());

        // 채널 목록 조회 (findAllByUserId)
        List<ChannelResponse> userChannels = channelService.findAllByUserId(aId);
        System.out.println(">> 유저A가 볼 수 있는 채널 수: " + userChannels.size());

        // PRIVATE 채널 조회 시 참여자 목록 확인
        ChannelResponse foundPrivate = channelService.findById(privateChannel.getId());
        System.out.println(">> PRIVATE 채널 참여자: " + foundPrivate.participantIds());

        // ==================== 3. 수정 테스트 ====================
        System.out.println("\n[ 3. 수정 테스트 ]");
        System.out.println("----------------------------------------");

        // 유저 수정
        System.out.println(">> 수정 전 유저 닉네임: " + userService.findById(aId).nickname());
        UserUpdateRequest updateRequest = new UserUpdateRequest(
                null,
                null,
                "수정된닉네임",
                null
        );
        UserResponse updatedUser = userService.update(aId, updateRequest, null);
        System.out.println(">> 수정 후 유저 닉네임: " + updatedUser.nickname());

        // 프로필 이미지 교체
        BinaryContentCreateRequest newProfile = new BinaryContentCreateRequest(
                "new_profile.jpg",
                "image/jpeg",
                new byte[]{4, 5, 6}
        );
        UserUpdateRequest profileUpdateRequest = new UserUpdateRequest(null, null, null, null);
        UserResponse updatedUserC = userService.update(userC.userId(), profileUpdateRequest, newProfile);
        System.out.println(">> 프로필 교체 후 이미지 ID: " + updatedUserC.profileImageId());

        // 채널 수정 (PUBLIC만 가능)
        System.out.println(">> 수정 전 채널 이름: " + channelService.findById(channelId).name());
        ChannelUpdateRequest channelUpdateRequest = new ChannelUpdateRequest(
                "수정된채널이름",
                "수정된설명"
        );
        Channel updatedChannel = channelService.update(channelId, channelUpdateRequest);
        System.out.println(">> 수정 후 채널 이름: " + updatedChannel.getName());

        // PRIVATE 채널 수정 시도 (예외 발생해야 함)
        try {
            ChannelUpdateRequest privateUpdateRequest = new ChannelUpdateRequest("이름변경", null);
            channelService.update(privateChannel.getId(), privateUpdateRequest);
            System.out.println(">> [실패] PRIVATE 채널 수정됨");
        } catch (Exception e) {
            System.out.println(">> [성공] PRIVATE 채널 수정 불가: " + e.getMessage());
        }

        // ==================== 4. 로그인 테스트 ====================
        System.out.println("\n[ 4. 로그인 테스트 ]");
        System.out.println("----------------------------------------");

        // 로그인 성공
        LoginRequest loginRequest = new LoginRequest("a", "pw");
        UserResponse loggedInUser = authService.login(loginRequest);
        System.out.println(">> 로그인 성공: " + loggedInUser.username());

        // 로그인 실패 (잘못된 비밀번호)
        try {
            LoginRequest wrongPw = new LoginRequest("a", "wrongpw");
            authService.login(wrongPw);
            System.out.println(">> [실패] 잘못된 비밀번호로 로그인됨");
        } catch (Exception e) {
            System.out.println(">> [성공] 로그인 실패: " + e.getMessage());
        }

        // ==================== 5. 삭제 테스트 ====================
        System.out.println("\n[ 5. 삭제 테스트 ]");
        System.out.println("----------------------------------------");

        System.out.println(">> 삭제 전 유저 수: " + userService.findAll().size());
        userService.hardDelete(userC.userId());
        System.out.println(">> 삭제 후 유저 수: " + userService.findAll().size());

        // 삭제된 유저 조회 시도
        try {
            userService.findById(userC.userId());
            System.out.println(">> [실패] 삭제된 유저가 조회됨");
        } catch (Exception e) {
            System.out.println(">> [성공] 삭제된 유저 조회 불가: " + e.getMessage());
        }

        // ==================== 6. 중복 검증 테스트 ====================
        System.out.println("\n[ 6. 중복 검증 테스트 ]");
        System.out.println("----------------------------------------");

        // username 중복
        try {
            UserCreateRequest dupUsername = new UserCreateRequest("a", "new@test.com", "pw", "새유저");
            userService.create(dupUsername, null);
            System.out.println(">> [실패] 중복 username 예외 발생 안함");
        } catch (Exception e) {
            System.out.println(">> [성공] username 중복: " + e.getMessage());
        }

        // email 중복
        try {
            UserCreateRequest dupEmail = new UserCreateRequest("newuser", "a@test.com", "pw", "새유저");
            userService.create(dupEmail, null);
            System.out.println(">> [실패] 중복 email 예외 발생 안함");
        } catch (Exception e) {
            System.out.println(">> [성공] email 중복: " + e.getMessage());
        }

        System.out.println("\n========== 테스트 완료 ==========");
    }
}
