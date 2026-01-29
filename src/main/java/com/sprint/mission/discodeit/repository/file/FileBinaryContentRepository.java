package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Repository
public class FileBinaryContentRepository implements BinaryContentRepository {

    private static final Path FILE_PATH = Paths.get("data", "binaryContents.ser");

    public FileBinaryContentRepository() {
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
    private Map<UUID, BinaryContent> loadFromFile() {
        if (Files.notExists(FILE_PATH)) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(FILE_PATH))) {
            return (Map<UUID, BinaryContent>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("BinaryContent 데이터 로드 실패", e);
        }
    }

    private void saveToFile(Map<UUID, BinaryContent> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(FILE_PATH))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("BinaryContent 데이터 저장 실패", e);
        }
    }

    // ============================================
    // Repository 구현
    // ============================================
    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        Map<UUID, BinaryContent> data = loadFromFile();
        data.put(binaryContent.getId(), binaryContent);
        saveToFile(data);
        return binaryContent;
    }

    @Override
    public Optional<BinaryContent> findById(UUID binaryContentId) {
        Map<UUID, BinaryContent> data = loadFromFile();
        return Optional.ofNullable(data.get(binaryContentId));
    }

    @Override
    public List<BinaryContent> findAll() {
        return new ArrayList<>(loadFromFile().values());
    }

    @Override
    public void deleteById(UUID binaryContentId) {
        Map<UUID, BinaryContent> data = loadFromFile();
        data.remove(binaryContentId);
        saveToFile(data);
    }

    @Override
    public boolean existsById(UUID binaryContentId) {
        return loadFromFile().containsKey(binaryContentId);
    }
}
