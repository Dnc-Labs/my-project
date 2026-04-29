package com.ecommerce.api.services;

import com.ecommerce.api.dto.response.ProductImageResponse;
import com.ecommerce.api.repository.ProductImageRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.storage.FileValidator;
import com.ecommerce.api.storage.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ProductImageService {

    private static final int MAX_IMAGES_PER_PRODUCT = 8;
    private static final String STORAGE_FOLDER = "products";

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final StorageService storageService;
    private final FileValidator fileValidator;

    public ProductImageService(ProductImageRepository imageRepository,
                                ProductRepository productRepository,
                                StorageService storageService,
                                FileValidator fileValidator) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
        this.storageService = storageService;
        this.fileValidator = fileValidator;
    }

    /**
     * TODO: Upload ảnh cho product
     *
     * 1. Tìm product theo productId → throw ResourceNotFoundException nếu không có
     * 2. Validate file: fileValidator.validateImage(file)
     * 3. Check limit: nếu count ảnh hiện tại của product >= MAX_IMAGES_PER_PRODUCT
     *    → throw exception (để tránh spam)
     * 4. Upload file lên storage:
     *    UploadResult result = storageService.upload(file, STORAGE_FOLDER);
     * 5. Tạo ProductImage entity:
     *    - key = result.key()
     *    - url = result.url()
     *    - originalName = file.getOriginalFilename()
     *    - product = product (set entity)
     *    - createdAt = LocalDateTime.now()
     *    - isPrimary = (count hiện tại == 0) — ảnh đầu tiên auto primary
     * 6. Save và trả ProductImageResponse
     *
     * Lưu ý transaction: nếu storage upload thành công nhưng DB save fail
     *                    → file rác trên storage. Production cần xử lý cleanup
     *                    (compensating transaction). Giờ skip cho đơn giản.
     */
    public ProductImageResponse uploadImage(Long productId, MultipartFile file) {
        // TODO: Triển khai
        return null;
    }

    /**
     * TODO: List tất cả ảnh của product (public)
     */
    public List<ProductImageResponse> getImagesByProduct(Long productId) {
        // TODO: Check product tồn tại + return list
        return null;
    }

    /**
     * TODO: Set ảnh isPrimary
     *
     * 1. Tìm image mới theo imageId → throw nếu không có
     * 2. Tìm image primary hiện tại của cùng product
     * 3. Nếu primary cũ != null && primary cũ != image mới → set isPrimary=false cho cũ
     * 4. Set image mới isPrimary=true
     * 5. Save cả 2 (hoặc dùng @Transactional)
     */
    public ProductImageResponse setPrimary(Long imageId) {
        // TODO: Triển khai
        return null;
    }

    /**
     * TODO: Xoá ảnh
     *
     * 1. Tìm image theo imageId
     * 2. Xoá file trên storage: storageService.delete(image.getKey())
     * 3. Xoá record DB
     *
     * Lưu ý thứ tự: xoá storage trước, DB sau.
     * Nếu DB fail → còn record trỏ tới file đã xoá → broken.
     * Pattern an toàn: dùng @Transactional + xoá DB trước, storage sau (afterCommit hook).
     * Skip cho đơn giản giai đoạn này.
     */
    public void deleteImage(Long imageId) {
        // TODO: Triển khai
    }
}
