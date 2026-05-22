package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.CreateProductVariantRequest;
import com.ecommerce.api.dto.request.UpdateProductVariantRequest;
import com.ecommerce.api.dto.response.ProductVariantResponse;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.ProductVariant;
import com.ecommerce.api.exception.DuplicateResource;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.mapper.ProductVariantMapper;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.ProductVariantRepository;
import com.ecommerce.api.utilities.CheckData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final ProductVariantMapper variantMapper;

    public ProductVariantResponse createVariant(Long productId, CreateProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (variantRepository.existsBySku(request.getSku())) {
            throw new DuplicateResource("SKU already exists");
        }
        if (variantRepository.existsByProductIdAndSizeAndColor(productId, request.getSize(), request.getColor())) {
            throw new DuplicateResource("Variant already exists");
        }

        ProductVariant variant = variantMapper.fromRequestDto(request);
        variant.setProduct(product);
        ProductVariant saved = variantRepository.save(variant);
        return variantMapper.fromEntity(saved);
    }

    public List<ProductVariantResponse> getVariantsByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }
        List<ProductVariant> variants = variantRepository.findByProductId(productId);
        return variants.stream().map(variantMapper::fromEntity).toList();
    }

    public ProductVariantResponse getVariantById(Long variantId) {
        ProductVariant variant = findVariantOrThrow(variantId);
        return variantMapper.fromEntity(variant);
    }

    /**
     * Cập nhật variant. Ownership check đã xử lý ở @PreAuthorize controller —
     * service chỉ tập trung vào business logic.
     */
    public ProductVariantResponse updateVariant(Long variantId, UpdateProductVariantRequest request) {
        ProductVariant variant = findVariantOrThrow(variantId);
        variantMapper.updateEntity(request,variant);

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
        return variantMapper.fromEntity(variantRepository.save(variant));
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
