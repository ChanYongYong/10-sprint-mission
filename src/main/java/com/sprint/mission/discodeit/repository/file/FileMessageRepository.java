package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
@Repository
public class FileMessageRepository implements MessageRepository {

    private static final Path FILE_PATH = Paths.get("data", "messages.ser");

    public FileMessageRepository() {
        initializeFile();
    }

    // ============================================
    // 파일 I/O
    // ============================================

    private void initializeFile() {
        try {
            if (Files.notExists(FILE_PATH.getParent())) {
                Files.createDirectories(FILE_PATH.getParent());
            }
            if (Files.notExists(FILE_PATH)) {
                saveToFile(new HashMap<>());
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 초기화 실패", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, Message> loadFromFile() {
        if (Files.notExists(FILE_PATH)) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(FILE_PATH))) {
            return (Map<UUID, Message>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("메시지 데이터 로드 실패", e);
        }
    }

    private void saveToFile(Map<UUID, Message> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(FILE_PATH))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("메시지 데이터 저장 실패", e);
        }
    }

    // ============================================
    // Repository 구현
    // ============================================

    @Override
    public Message save(Message message) {
        Map<UUID, Message> data = loadFromFile();
        data.put(message.getId(), message);
        saveToFile(data);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID messageId) {
        Map<UUID, Message> data = loadFromFile();
        return Optional.ofNullable(data.get(messageId));
    }



    @Override
    public List<Message> findAll() {
        return new ArrayList<>(loadFromFile().values());
    }

    @Override
    public void deleteById(UUID messageId) {
        Map<UUID, Message> data = loadFromFile();
        data.remove(messageId);
        saveToFile(data);
    }

    @Override
    public boolean existsById(UUID messageId) {
        return loadFromFile().containsKey(messageId);
    }

    // FileMessageRepository에 구현
    @Override
    public List<Message> findAllBySenderId(UUID senderId) {
        return findAll().stream()
                .filter(m -> m.getSenderId().equals(senderId))
                .toList();
    }
}
