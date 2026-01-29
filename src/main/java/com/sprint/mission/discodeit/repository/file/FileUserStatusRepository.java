package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Repository
public class FileUserStatusRepository implements UserStatusRepository {

    private static final Path FILE_PATH = Paths.get("data", "userStatuses.ser");

    public FileUserStatusRepository() {
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
    private Map<UUID, UserStatus> loadFromFile() {
        if (Files.notExists(FILE_PATH)) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(FILE_PATH))) {
            return (Map<UUID, UserStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("UserStatus 데이터 로드 실패", e);
        }
    }

    private void saveToFile(Map<UUID, UserStatus> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(FILE_PATH))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("UserStatus 데이터 저장 실패", e);
        }
    }

    // ============================================
    // Repository 구현
    // ============================================
    @Override
    public UserStatus save(UserStatus userStatus) {
        Map<UUID, UserStatus> data = loadFromFile();
        data.put(userStatus.getId(), userStatus);
        saveToFile(data);
        return userStatus;
    }

    @Override
    public Optional<UserStatus> findById(UUID userStatusId) {
        Map<UUID, UserStatus> data = loadFromFile();
        return Optional.ofNullable(data.get(userStatusId));
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return loadFromFile().values().stream()
                .filter(us -> us.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public List<UserStatus> findAll() {
        return new ArrayList<>(loadFromFile().values());
    }

    @Override
    public void deleteById(UUID userStatusId) {
        Map<UUID, UserStatus> data = loadFromFile();
        data.remove(userStatusId);
        saveToFile(data);
    }

    @Override
    public boolean existsById(UUID userStatusId) {
        return loadFromFile().containsKey(userStatusId);
    }
}
