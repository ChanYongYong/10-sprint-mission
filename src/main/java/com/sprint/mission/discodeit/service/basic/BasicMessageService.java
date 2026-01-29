package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.InvalidRequestException;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;        //  변경
    private final ChannelRepository channelRepository;  //  변경


    @Override
    public Message create(String content, UUID senderId, UUID channelId) {
        if (content == null || content.isBlank()) {
            throw new InvalidRequestException("content", "null이 아니고 빈 값이 아님", content);
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 유저", senderId));
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 채널", channelId));

        Message message = new Message(content, senderId, channelId);
        channel.addMessage(message.getId());
        sender.addMessage(message.getId());

        messageRepository.save(message);
        userRepository.save(sender);
        channelRepository.save(channel);

        return message;
    }

    @Override
    public Message findById(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 메시지", messageId));
    }

    @Override
    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    @Override
    public List<Message> findByChannel(UUID channelId) {
        channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 채널", channelId));

        return messageRepository.findAll().stream()
                .filter(message -> !message.isDeletedAt())
                .filter(message -> message.getChannelId().equals(channelId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findBySender(UUID senderId) {
        userRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("id", "존재하는 유저", senderId));

        return messageRepository.findAll().stream()
                .filter(message -> !message.isDeletedAt())
                .filter(message -> message.getSenderId().equals(senderId))
                .collect(Collectors.toList());
    }

    @Override
    public Message update(UUID messageId, String content) {
        Message message = findById(messageId);
        message.updateContent(content);
        message.updateEditedAt(true);
        return messageRepository.save(message);
    }

    @Override
    public void softDelete(UUID messageId) {
        Message message = findById(messageId);
        message.updateDeletedAt(true);
        messageRepository.save(message);
    }

    @Override
    public void hardDelete(UUID messageId) {
        Message message = findById(messageId);
        User sender = userRepository.findById(message.getSenderId())
                .orElseThrow(()-> new NotFoundException("userId","존재하는 유저",message.getSenderId()));

        Channel channel = channelRepository.findById(message.getChannelId())
                .orElseThrow(()-> new NotFoundException("channelId","존재하는 채널",message.getChannelId()));

        sender.removeMessage(messageId);
        channel.removeMessage(messageId);

        messageRepository.deleteById(messageId);
        userRepository.save(sender);      // 추가
        channelRepository.save(channel);  // 추가

    }
}
