package com.ecommerce.api.services;

import java.util.List;
import java.util.Optional;
import com.ecommerce.api.mapper.ProductImageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
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
     * Note: nếu storage upload thành công nhưng DB save fail → file rác trên
     * storage. Cần compensating transaction cho production. Giờ skip cho đơn giản.
     */
    @Transactional
    public ProductImageResponse uploadImage(Long productId, MultipartFile file) {
        log.info("Upload image for product : {}", productId);
        Product product = this.productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException(
                "Product not found"));
        this.fileValidator.validateImage(file);
        long cntImagesPerProduct = this.imageRepository.countByProductId(productId);
        if (cntImagesPerProduct >= MAX_IMAGES_PER_PRODUCT)
            throw new BusinessRuleException("Product has reached the image limit (max " + MAX_IMAGES_PER_PRODUCT + ")");
        UploadResult result = storageService.upload(file, STORAGE_FOLDER);
        ProductImage image = new ProductImage();
        image.setStorageKey(result.key());
        image.setUrl(result.url());
        image.setOriginalName(file.getOriginalFilename());
        image.setProduct(product);
        image.setIsPrimary(cntImagesPerProduct == 0);
        ProductImage saved = imageRepository.save(image);
        log.info("Upload image successfully for product {} with url {}", productId, result.url());
        return imageMapper.fromEntity(saved);
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
