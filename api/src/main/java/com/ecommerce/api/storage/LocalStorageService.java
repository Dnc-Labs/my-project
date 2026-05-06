package com.ecommerce.api.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation lưu file vào filesystem local.
 * <p>
 * Chỉ active khi storage.type=local trong application.yaml.
 * Spring sẽ KHÔNG tạo bean này nếu storage.type là 's3' hoặc khác.
 * <p>
 * Lưu ý: storage.type=local là default — nếu property không khai báo,
 * matchIfMissing=true sẽ vẫn active bean này.
 */
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final String uploadDir;
    private final String publicUrlPrefix;
    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    public LocalStorageService(
            @Value("${storage.local.upload-dir}") String uploadDir,
            @Value("${storage.local.public-url-prefix}") String publicUrlPrefix) {
        this.uploadDir = uploadDir;
        this.publicUrlPrefix = publicUrlPrefix;
    }

    /**
     * Filename = UUID + extension để chống Path Traversal — KHÔNG concat
     * originalFilename vào path. Streaming Files.copy để tránh OOM với file lớn.
     */
    @Override
    public UploadResult upload(MultipartFile file, String folder) {
        Path targetFolder = Paths.get(uploadDir, folder);
        String ext = getExtension(file.getOriginalFilename());

        if (ext.isBlank()) throw new RuntimeException("File must have a valid extension");

        String fileName = UUID.randomUUID() + ext;

        Path target = targetFolder.resolve(fileName);
        try {
            Files.createDirectories(targetFolder);
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + fileName, e);
        }

        String key = folder + "/" + fileName;
        String url = publicUrlPrefix + "/" + key;
        return new UploadResult(key, url);
    }

    @Override
    public void delete(String key) {
        Path target = Paths.get(uploadDir, key);
        try {
            boolean deleted = Files.deleteIfExists(target);
            if (!deleted) {
                log.warn("Tried to delete non-existent file: {}", key);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + target, e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int index = filename.lastIndexOf('.');
        if (index <= 0) return "";
        return filename.substring(index);
    }
}
