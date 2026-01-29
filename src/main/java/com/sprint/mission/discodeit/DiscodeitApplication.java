package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

        UserService userService = context.getBean(UserService.class);
        ChannelService channelService = context.getBean(ChannelService.class);
        MessageService messageService = context.getBean(MessageService.class);

        // ==================== 1. 등록 테스트 ====================
        System.out.println("\n[ 1. 등록 테스트 ]");
        System.out.println("----------------------------------------");

        // 유저 등록 (DTO 사용)
        UserCreateRequest userARequest = new UserCreateRequest("a", "a@test.com", "pw", "유저A");
        UserCreateRequest userBRequest = new UserCreateRequest("b", "b@test.com", "pw", "유저B");

        UserResponse userA = userService.create(userARequest, null);  // 프로필 이미지 없이
        UserResponse userB = userService.create(userBRequest, null);

        UUID aId = userA.id();
        UUID bId = userB.id();

        System.out.println(">> 유저 등록 완료: " + userService.findAll().stream()
                .map(UserResponse::username).toList());
        System.out.println(">> 유저A 온라인 상태: " + userA.online());

        // 프로필 이미지와 함께 유저 등록 테스트
        UserCreateRequest userCRequest = new UserCreateRequest("c", "c@test.com", "pw", "유저C");
        BinaryContentCreateRequest profileRequest = new BinaryContentCreateRequest(
                "profile.png",
                "image/png",
                new byte[]{1, 2, 3}  // 테스트용 더미 데이터
        );
        UserResponse userC = userService.create(userCRequest, profileRequest);
        System.out.println(">> 프로필 이미지 있는 유저 등록: " + userC.username());
        System.out.println(">> 프로필 이미지 ID: " + userC.profileImageId());

        // 채널 등록
        Channel channel = channelService.create("테스트채널", "설명", ChannelType.PUBLIC, aId);
        UUID channelId = channel.getId();
        channelService.addMember(channelId, bId);
        System.out.println(">> 채널 등록 완료: " + channelService.findAll().stream()
                .map(Channel::getName).toList());

        // 메시지 등록
        Message msg1 = messageService.create("첫번째 메시지", aId, channelId);
        Message msg2 = messageService.create("두번째 메시지", bId, channelId);
        UUID msg1Id = msg1.getId();
        System.out.println(">> 메시지 등록 완료: " + messageService.findAll().stream()
                .map(Message::getContent).toList());

        // ==================== 2. 조회 테스트 ====================
        System.out.println("\n[ 2. 조회 테스트 ]");
        System.out.println("----------------------------------------");

        // 단건 조회 (UserResponse 반환)
        UserResponse foundUser = userService.findById(aId);
        System.out.println(">> 유저 단건 조회: " + foundUser.username());
        System.out.println(">> 유저 온라인 상태: " + foundUser.online());
        System.out.println(">> 유저 이메일: " + foundUser.email());

        // 다건 조회
        System.out.println(">> 유저 다건 조회: " + userService.findAll().size() + "명");

        // ==================== 3. 수정 테스트 ====================
        System.out.println("\n[ 3. 수정 테스트 ]");
        System.out.println("----------------------------------------");

        // 유저 수정 (DTO 사용)
        System.out.println(">> 수정 전 유저 닉네임: " + userService.findById(aId).nickname());

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                aId,
                null,           // username 변경 안함
                null,           // email 변경 안함
                "수정된닉네임",   // nickname 변경
                null            // password 변경 안함
        );
        UserResponse updatedUser = userService.update(updateRequest, null);
        System.out.println(">> 수정 후 유저 닉네임: " + updatedUser.nickname());

        // 프로필 이미지 교체 테스트
        BinaryContentCreateRequest newProfile = new BinaryContentCreateRequest(
                "new_profile.jpg",
                "image/jpeg",
                new byte[]{4, 5, 6}
        );
        UserUpdateRequest profileUpdateRequest = new UserUpdateRequest(
                userC.id(), null, null, null, null
        );
        UserResponse updatedUserC = userService.update(profileUpdateRequest, newProfile);
        System.out.println(">> 프로필 교체 후 이미지 ID: " + updatedUserC.profileImageId());

        // ==================== 4. 삭제 테스트 ====================
        System.out.println("\n[ 4. 삭제 테스트 ]");
        System.out.println("----------------------------------------");

        System.out.println(">> 삭제 전 유저 수: " + userService.findAll().size());
        userService.hardDelete(userC.id());  // 프로필 이미지 + UserStatus도 같이 삭제됨
        System.out.println(">> 삭제 후 유저 수: " + userService.findAll().size());

        // 삭제된 유저 조회 시도
        try {
            userService.findById(userC.id());
            System.out.println(">> [실패] 삭제된 유저가 조회됨");
        } catch (Exception e) {
            System.out.println(">> [성공] 삭제된 유저 조회 불가: " + e.getMessage());
        }

        // ==================== 5. 중복 검증 테스트 ====================
        System.out.println("\n[ 5. 중복 검증 테스트 ]");
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
