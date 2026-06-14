package com.ecommerce.api.services;

import java.util.List;
import java.util.Optional;
import com.ecommerce.api.mapper.ProductImageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ecommerce.api.dto.response.ProductImageResponse;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.ProductImage;
import com.ecommerce.api.exception.BusinessRuleException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.ProductImageRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.storage.FileValidator;
import com.ecommerce.api.storage.StorageService;
import com.ecommerce.api.storage.UploadResult;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductImageService {

    private static final int MAX_IMAGES_PER_PRODUCT = 8;
    private static final String STORAGE_FOLDER = "products";

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final StorageService storageService;
    private final FileValidator fileValidator;
    private final ProductImageMapper imageMapper;


    /**
     * Upload ảnh cho product. Ảnh đầu tiên (count == 0) auto trở thành primary.
     *
     * NOT_SUPPORTED: chạy NGOÀI transaction để KHÔNG giữ DB connection suốt thời gian
     * upload I/O chậm (5-10s) → tránh connection starvation. imageRepository.save() tự
     * mở transaction ngắn riêng (Spring Data REQUIRED). Pattern store-then-record: upload
     * trước, ghi DB sau; nếu DB fail thì đền bù bằng cách xoá file (compensating action).
     * Image-limit là SOFT limit — chấp nhận race count (không có DB constraint enforce).
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ProductImageResponse uploadImage(Long productId, MultipartFile file) {
        log.info("Upload image for product : {}", productId);

        // 1. Reads + validate (mỗi read tự auto-commit, không giữ connection lâu)
        Product product = this.productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException(
                "Product not found"));
        this.fileValidator.validateImage(file);
        long cntImagesPerProduct = this.imageRepository.countByProductId(productId);
        if (cntImagesPerProduct >= MAX_IMAGES_PER_PRODUCT)
            throw new BusinessRuleException("Product has reached the image limit (max " + MAX_IMAGES_PER_PRODUCT + ")");

        // 2. Upload lên storage — NGOÀI transaction
        UploadResult result = storageService.upload(file, STORAGE_FOLDER);

        try {
            ProductImage productImage = new ProductImage();
            productImage.setStorageKey(result.key());
            productImage.setUrl(result.url());
            productImage.setProduct(product);
            productImage.setOriginalName(file.getOriginalFilename());
            productImage.setIsPrimary(cntImagesPerProduct == 0);
            ProductImage saved = imageRepository.save(productImage);
            log.info("Upload image successfully for product {} with url {}", productId, result.url());
            return imageMapper.fromEntity(saved);
        } catch (Exception e) {
            // ĐỀN BÙ: xoá file vừa upload (best-effort) rồi rethrow ĐÚNG exception gốc —
            // giữ nguyên type để GlobalExceptionHandler trả đúng status (vd DataIntegrityViolation → 409).
            safeDelete(result.key());
            throw e;
        }
    }

    /**
     * Xoá file storage best-effort khi compensating. KHÔNG ném exception ra ngoài —
     * lỗi xoá không được che lỗi gốc. Hết retry thì log WARN kèm key để job dọn rác/người
     * vận hành tìm lại file orphan.
     */
    private void safeDelete(String storageKey) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                storageService.delete(storageKey);
                return;
            } catch (Exception e) {
                // log cause mỗi lần fail để debug; chưa hết retry thì thử lại
                log.warn("Compensating delete failed (attempt {}/{}) for key {}: {}",
                        attempt, maxAttempts, storageKey, e.getMessage());
            }
        }
        log.warn("Orphan file left on storage after {} failed delete attempts: {}", maxAttempts, storageKey);
    }

    public List<ProductImageResponse> getImagesByProduct(Long productId) {
        if (!productRepository.existsById(productId)) throw new ResourceNotFoundException("Product not found");
        List<ProductImage> images = this.imageRepository.findByProductId(productId);
        return images.stream().map(imageMapper::fromEntity).toList();
    }

    /**
     * Set ảnh isPrimary cho 1 product. Idempotent (gọi nhiều lần với cùng imageId
     * không gây side effect). Dùng @Transactional + dirty checking để auto save.
     */
    @Transactional
    public ProductImageResponse setPrimary(Long imageId) {
        log.info("Setting primary image: id={}", imageId);
        ProductImage image = imageRepository.findById(imageId).orElseThrow(() -> new ResourceNotFoundException(
                "Image not found"));
        Optional<ProductImage> primaryImage = imageRepository.findByProductIdAndIsPrimaryTrue(image.getProduct().getId());
        // Đã là primary rồi → no-op
        if (primaryImage.isPresent() && primaryImage.get().getId().equals(image.getId()))
            return imageMapper.fromEntity(image);
        // Set image mới = primary
        image.setIsPrimary(true);
        primaryImage.ifPresent(old -> old.setIsPrimary(false));
        return imageMapper.fromEntity(image);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        log.info("Deleting image: id={}", imageId);
        ProductImage image = imageRepository.findById(imageId).orElseThrow(() -> new ResourceNotFoundException(
                "Image not found"));
        String key = image.getStorageKey();
        imageRepository.delete(image);
        log.info("Image deleted successfully: id={}", imageId);
        // Note: storage.delete() throws RuntimeException nếu fail → @Transactional rollback DB.
        // An toàn cho local filesystem (atomic). Khi migrate sang S3, cần đổi sang
        // TransactionSynchronization.afterCommit() để tránh broken-link khi network fail.
        storageService.delete(key);
    }
}
