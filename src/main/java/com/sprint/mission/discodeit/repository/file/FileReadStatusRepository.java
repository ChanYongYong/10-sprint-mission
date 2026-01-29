package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Repository
public class FileReadStatusRepository implements ReadStatusRepository {
    private static final Path FILE_PATH = Paths.get("data", "readstatus.ser");

    public FileReadStatusRepository() {
        initializeFile();
    }

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
    private Map<UUID, ReadStatus> loadFromFile() {
        if (Files.notExists(FILE_PATH)) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(FILE_PATH))) {
            return (Map<UUID, ReadStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("ReadStatus 데이터 로드 실패", e);
        }
    }

    private void saveToFile(Map<UUID, ReadStatus> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(FILE_PATH))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("ReadStatus 데이터 저장 실패", e);
        }
    }

    @Override
    public ReadStatus save(ReadStatus readStatus) {
        Map<UUID, ReadStatus> data = loadFromFile();
        data.put(readStatus.getId(), readStatus);
        saveToFile(data);
        return readStatus;
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return Optional.ofNullable(loadFromFile().get(id));
    }

    @Override
    public List<ReadStatus> findAll() {
        return new ArrayList<>(loadFromFile().values());
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        return findAll().stream()
                .filter(rs -> rs.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        Map<UUID, ReadStatus> data = loadFromFile();
        data.remove(id);
        saveToFile(data);
    }

    @Override
    public boolean existsById(UUID id) {
        return loadFromFile().containsKey(id);
    }
}
