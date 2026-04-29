package com.ecommerce.api.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation lưu file vào filesystem local.
 *
 * Chỉ active khi storage.type=local trong application.yaml.
 * Spring sẽ KHÔNG tạo bean này nếu storage.type là 's3' hoặc khác.
 *
 * Lưu ý: storage.type=local là default — nếu property không khai báo,
 * matchIfMissing=true sẽ vẫn active bean này.
 */
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final String uploadDir;
    private final String publicUrlPrefix;

    public LocalStorageService(
            @Value("${storage.local.upload-dir}") String uploadDir,
            @Value("${storage.local.public-url-prefix}") String publicUrlPrefix) {
        this.uploadDir = uploadDir;
        this.publicUrlPrefix = publicUrlPrefix;
    }

    /**
     * TODO: Triển khai upload
     *
     * 1. Tạo folder đích nếu chưa có:
     *    Path targetFolder = Paths.get(uploadDir, folder);
     *    Files.createDirectories(targetFolder);
     *
     * 2. Generate UUID-based filename giữ extension gốc:
     *    String ext = getExtension(file.getOriginalFilename());  // ".jpg"
     *    String filename = UUID.randomUUID() + ext;
     *
     * 3. Tạo path đầy đủ:
     *    Path target = targetFolder.resolve(filename);
     *
     *    ⚠️ BẢO MẬT: KHÔNG concat originalFilename vào path → Path Traversal.
     *    Dùng UUID là an toàn nhất.
     *
     * 4. Copy file:
     *    file.transferTo(target);
     *    HOẶC streaming để tránh OOM với file lớn:
     *    Files.copy(file.getInputStream(), target);
     *
     * 5. Trả UploadResult:
     *    - key = folder + "/" + filename  (vd: "products/uuid.jpg")
     *    - url = publicUrlPrefix + "/" + key  (vd: "/uploads/products/uuid.jpg")
     *
     * Wrap IOException trong RuntimeException (StorageException) — service
     * không nên expose checked exception lên controller.
     */
    @Override
    public UploadResult upload(MultipartFile file, String folder) {
        // TODO: Triển khai theo hướng dẫn ở docstring
        return null;
    }

    /**
     * TODO: Triển khai delete
     *
     * 1. Resolve path từ key:
     *    Path target = Paths.get(uploadDir, key);
     *
     * 2. Files.deleteIfExists(target);
     *    Dùng deleteIfExists thay vì delete để không throw nếu file đã bị xoá
     *
     * 3. Wrap IOException
     */
    @Override
    public void delete(String key) {
        // TODO: Triển khai
    }

    /**
     * TODO: Helper lấy extension từ filename
     * - "nike.jpg" → ".jpg"
     * - "no-extension" → ""
     * - null → ""
     *
     * Dùng String.lastIndexOf('.')
     */
    private String getExtension(String filename) {
        // TODO: Triển khai
        return "";
    }
}
