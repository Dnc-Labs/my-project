package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.CreateProductVariantRequest;
import com.ecommerce.api.dto.request.UpdateProductVariantRequest;
import com.ecommerce.api.dto.response.ProductVariantResponse;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.ProductVariant;
import com.ecommerce.api.exception.DuplicateResource;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.ProductVariantRepository;
import com.ecommerce.api.utilities.CheckData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    public ProductVariantService(ProductVariantRepository variantRepository,
                                  ProductRepository productRepository) {
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
    }

    public ProductVariantResponse createVariant(Long productId, CreateProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (variantRepository.existsBySku(request.getSku())) {
            throw new DuplicateResource("SKU already exists");
        }
        if (variantRepository.existsByProductIdAndSizeAndColor(productId, request.getSize(), request.getColor())) {
            throw new DuplicateResource("Variant already exists");
        }

        ProductVariant variant = new ProductVariant();
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setSku(request.getSku());
        variant.setImageUrl(request.getImageUrl());
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStock());
        variant.setProduct(product);

        ProductVariant saved = variantRepository.save(variant);
        return ProductVariantResponse.fromEntity(saved);
    }

    public List<ProductVariantResponse> getVariantsByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }
        List<ProductVariant> variants = variantRepository.findByProductId(productId);
        return variants.stream().map(ProductVariantResponse::fromEntity).toList();
    }

    public ProductVariantResponse getVariantById(Long variantId) {
        ProductVariant variant = findVariantOrThrow(variantId);
        return ProductVariantResponse.fromEntity(variant);
    }

    /**
     * Cập nhật variant. Ownership check đã xử lý ở @PreAuthorize controller —
     * service chỉ tập trung vào business logic.
     */
    public ProductVariantResponse updateVariant(Long variantId, UpdateProductVariantRequest request) {
        ProductVariant variant = findVariantOrThrow(variantId);

        String newSku = request.getSku();
        if (CheckData.checkIsNotNull(newSku) && !newSku.equals(variant.getSku())) {
            if (variantRepository.existsBySku(newSku)) {
                throw new DuplicateResource("SKU already exists");
            }
            variant.setSku(newSku);
        }

        String newSize = request.getSize();
        String newColor = request.getColor();
        boolean sizeChanged = CheckData.checkIsNotNull(newSize) && !newSize.equals(variant.getSize());
        boolean colorChanged = CheckData.checkIsNotNull(newColor) && !newColor.equals(variant.getColor());
        if (sizeChanged || colorChanged) {
            String size = sizeChanged ? newSize : variant.getSize();
            String color = colorChanged ? newColor : variant.getColor();
            if (variantRepository.existsByProductIdAndSizeAndColor(variant.getProduct().getId(), size, color)) {
                throw new DuplicateResource("Variant already exists");
            }
            variant.setSize(size);
            variant.setColor(color);
        }

        if (CheckData.checkIsNotNull(request.getImageUrl())) variant.setImageUrl(request.getImageUrl());
        if (CheckData.checkIsNotNull(request.getStock())) variant.setStock(request.getStock());
        if (CheckData.checkIsNotNull(request.getPrice())) variant.setPrice(request.getPrice());

        return ProductVariantResponse.fromEntity(variantRepository.save(variant));
    }

    public void deleteVariant(Long variantId) {
        ProductVariant variant = findVariantOrThrow(variantId);
        variantRepository.delete(variant);
    }

    private ProductVariant findVariantOrThrow(Long variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant not found"));
    }
}
