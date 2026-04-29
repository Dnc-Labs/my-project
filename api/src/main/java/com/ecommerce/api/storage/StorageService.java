package com.ecommerce.api.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction cho storage layer.
 *
 * Code business chỉ phụ thuộc interface này. Implementation cụ thể
 * (LocalStorageService, S3StorageService, CloudinaryStorageService)
 * được Spring inject qua @ConditionalOnProperty.
 *
 * Đây là Strategy Pattern + Dependency Inversion Principle (D trong SOLID):
 *   "Code phụ thuộc vào abstraction, không phụ thuộc concrete implementation."
 *
 * Khi muốn đổi storage backend (ví dụ từ local sang S3) → chỉ đổi
 * application.yaml (storage.type), KHÔNG sửa code business.
 */
public interface StorageService {

    /**
     * Upload file lên storage.
     *
     * @param file file client gửi qua multipart
     * @param folder folder con trong storage (ví dụ "products")
     * @return UploadResult chứa key (định danh internal) và url (public access)
     */
    UploadResult upload(MultipartFile file, String folder);

    /**
     * Xoá file theo key.
     */
    void delete(String key);
}
