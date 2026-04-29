package com.ecommerce.api.storage;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * Validator cho file upload — check thực sự là image (qua magic bytes),
 * không tin Content-Type hay extension do client gửi.
 *
 * Apache Tika đọc vài byte đầu của file → detect content type thực.
 * Đây là cách CHUẨN chống file spoofing.
 *
 * Ví dụ:
 *   Client gửi virus.exe rename thành nike.jpg
 *   → Content-Type "image/jpeg" (do client tự khai báo)
 *   → Extension ".jpg"
 *   → Magic bytes: bắt đầu bằng "MZ" (executable signature)
 *   → Tika detect: "application/x-msdownload" → REJECT
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
     * TODO: Validate file là ảnh hợp lệ
     *
     * 1. Check file != null && !file.isEmpty()
     * 2. Detect content type bằng Tika:
     *    String detectedType = tika.detect(file.getInputStream());
     *
     *    ⚠️ Lưu ý: gọi getInputStream() lần này có thể "consume" stream.
     *    Một số impl của MultipartFile cho phép gọi nhiều lần (Spring's
     *    StandardMultipartFile cache trong memory hoặc temp file).
     *    Test kỹ trên project. Nếu lỗi, dùng file.getBytes() rồi
     *    tika.detect(bytes) — nhưng tốn RAM với file lớn.
     *
     * 3. Nếu detectedType KHÔNG nằm trong ALLOWED_IMAGE_TYPES:
     *    throw new InvalidFileException("Unsupported file type: " + detectedType);
     *
     * Note: Tạo InvalidFileException riêng, hoặc dùng RuntimeException tạm.
     */
    public void validateImage(MultipartFile file) {
        // TODO: Triển khai
    }
}
