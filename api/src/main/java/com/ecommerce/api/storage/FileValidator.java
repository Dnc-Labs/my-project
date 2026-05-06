package com.ecommerce.api.storage;

import java.io.IOException;
import java.util.Set;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.api.exception.InvalidFileException;

/**
 * Validator cho file upload — check thực sự là image (qua magic bytes),
 * không tin Content-Type hay extension do client gửi.
 * <p>
 * Apache Tika đọc vài byte đầu của file → detect content type thực.
 * Đây là cách CHUẨN chống file spoofing.
 * <p>
 * Ví dụ:
 * Client gửi virus.exe rename thành nike.jpg
 * → Content-Type "image/jpeg" (do client tự khai báo)
 * → Extension ".jpg"
 * → Magic bytes: bắt đầu bằng "MZ" (executable signature)
 * → Tika detect: "application/x-msdownload" → REJECT
 */
@Component
public class FileValidator {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Tika tika = new Tika();

    /**
     * Validate file là ảnh hợp lệ qua magic bytes (anti-spoofing).
     * <p>
     * ⚠️ Lưu ý: gọi getInputStream() có thể "consume" stream.
     * Spring's StandardMultipartFile cache trong memory hoặc temp file
     * → cho phép gọi getInputStream() nhiều lần. Nếu sau này đổi sang
     * implementation khác mà không cache → cần đổi sang tika.detect(bytes).
     */

    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new InvalidFileException("File is empty");
        try {
            String detectedType = tika.detect(file.getInputStream());
            if (!ALLOWED_IMAGE_TYPES.contains(detectedType))
                throw new InvalidFileException("Unsupported file type: " + detectedType);
        } catch (IOException e) {
            throw new InvalidFileException("Failed to detect file type");
        }
    }
}
