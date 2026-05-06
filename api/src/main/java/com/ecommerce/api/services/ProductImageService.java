package com.ecommerce.api.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
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
public class ProductImageService {

    private static final int MAX_IMAGES_PER_PRODUCT = 8;
    private static final String STORAGE_FOLDER = "products";

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final StorageService storageService;
    private final FileValidator fileValidator;

    public ProductImageService(
            ProductImageRepository imageRepository,
            ProductRepository productRepository,
            StorageService storageService,
            FileValidator fileValidator) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
        this.storageService = storageService;
        this.fileValidator = fileValidator;
    }

    /**
     * Upload ảnh cho product. Ảnh đầu tiên (count == 0) auto trở thành primary.
     *
     * Note: nếu storage upload thành công nhưng DB save fail → file rác trên
     * storage. Cần compensating transaction cho production. Giờ skip cho đơn giản.
     */
    public ProductImageResponse uploadImage(Long productId, MultipartFile file) {
        Product product = this.productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException(
                "Product not found"));
        this.fileValidator.validateImage(file);
        long cntImagesPerProduct = this.imageRepository.countByProductId(productId);
        if (cntImagesPerProduct >= MAX_IMAGES_PER_PRODUCT)
            throw new BusinessRuleException("Product has reached the image limit (max " + MAX_IMAGES_PER_PRODUCT + ")");
        UploadResult result = storageService.upload(file, STORAGE_FOLDER);
        ProductImage image = new ProductImage();
        image.setKey(result.key());
        image.setUrl(result.url());
        image.setOriginalName(file.getOriginalFilename());
        image.setProduct(product);
        image.setIsPrimary(cntImagesPerProduct == 0);
        image.setCreatedAt(LocalDateTime.now());
        ProductImage saved = imageRepository.save(image);
        return ProductImageResponse.fromEntity(saved);
    }

    public List<ProductImageResponse> getImagesByProduct(Long productId) {
        if (!productRepository.existsById(productId)) throw new ResourceNotFoundException("Product not found");
        List<ProductImage> images = this.imageRepository.findByProductId(productId);
        return images.stream().map(ProductImageResponse::fromEntity).toList();
    }

    /**
     * Set ảnh isPrimary cho 1 product. Idempotent (gọi nhiều lần với cùng imageId
     * không gây side effect). Dùng @Transactional + dirty checking để auto save.
     */
    @Transactional
    public ProductImageResponse setPrimary(Long imageId) {
        ProductImage image = imageRepository.findById(imageId).orElseThrow(() -> new ResourceNotFoundException(
                "Image not found"));
        Optional<ProductImage> primaryImage = imageRepository.findByProductIdAndIsPrimaryTrue(image.getProduct().getId());
        // Đã là primary rồi → no-op
        if (primaryImage.isPresent() && primaryImage.get().getId().equals(image.getId()))
            return ProductImageResponse.fromEntity(image);
        // Set image mới = primary
        image.setIsPrimary(true);
        primaryImage.ifPresent(old -> old.setIsPrimary(false));
        return ProductImageResponse.fromEntity(image);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        ProductImage image = imageRepository.findById(imageId).orElseThrow(() -> new ResourceNotFoundException(
                "Image not found"));
        String key = image.getKey();
        imageRepository.delete(image);
        // Note: storage.delete() throws RuntimeException nếu fail → @Transactional rollback DB.
        // An toàn cho local filesystem (atomic). Khi migrate sang S3, cần đổi sang
        // TransactionSynchronization.afterCommit() để tránh broken-link khi network fail.
        storageService.delete(key);
    }
}
