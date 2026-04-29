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

    /**
     * TODO: Tạo variant cho 1 product
     * 1. Tìm product theo productId → throw ResourceNotFoundException nếu không có
     * 2. Check sku đã tồn tại chưa → throw exception
     * 3. Check trùng (product_id + size + color) → throw exception
     *    (DB cũng có unique constraint nhưng check trước để trả lỗi rõ ràng hơn,
     *     thay vì để DB throw SQLIntegrityConstraintViolation)
     * 4. Tạo ProductVariant entity, set product
     * 5. Lưu, trả về ProductVariantResponse
     */
    public ProductVariantResponse createVariant(Long productId, CreateProductVariantRequest request) {
        Product product = this.productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        boolean checkExitsBySku = this.variantRepository.existsBySku(request.getSku());
        if(checkExitsBySku) throw new DuplicateResource("SKU already exists");
        boolean checkVariantDuplicate = this.variantRepository.existsByProductIdAndSizeAndColor(productId, request.getSize(), request.getColor());
        if(checkVariantDuplicate) throw new DuplicateResource("Variant already exists");

        ProductVariant productVariant = new ProductVariant();
        productVariant.setColor(request.getColor());
        productVariant.setSize(request.getSize());
        productVariant.setSku(request.getSku());
        productVariant.setImageUrl(request.getImageUrl());
        productVariant.setPrice(request.getPrice());
        productVariant.setStock(request.getStock());
        productVariant.setProduct(product);
        ProductVariant newProductVariant =  this.variantRepository.save(productVariant);
        return ProductVariantResponse.fromEntity(newProductVariant);
    }

    /**
     * TODO: Lấy danh sách variants của 1 product (public)
     * 1. Check product tồn tại
     * 2. findByProductId
     * 3. Trả về list (rỗng → empty list, không null)
     */
    public List<ProductVariantResponse> getVariantsByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }
        List<ProductVariant> productVariants = this.variantRepository.findByProductId(productId);
        return productVariants.stream().map(ProductVariantResponse::fromEntity).toList();
    }

    /**
     * TODO: Lấy chi tiết 1 variant (public)
     */
    public ProductVariantResponse getVariantById(Long variantId) {
        ProductVariant productVariant = this.getProductVariantById(variantId);
        return ProductVariantResponse.fromEntity(productVariant);
    }

    /**
     * TODO: Update variant
     * Ownership check đã ở @PreAuthorize controller, service chỉ business logic.
     *
     * 1. Tìm variant theo id
     * 2. Nếu đổi sku (khác sku hiện tại) → check trùng
     * 3. Nếu đổi size hoặc color → check composite unique
     *    (product_id + new_size + new_color không được trùng với variant khác)
     * 4. Update các field optional bằng CheckData.checkIsNotNull
     * 5. Lưu, trả về response
     */
    public ProductVariantResponse updateVariant(Long variantId, UpdateProductVariantRequest request) {
        ProductVariant productVariant = this.getProductVariantById(variantId);

        if(CheckData.checkIsNotNull(request.getSku()) && !request.getSku().equals(productVariant.getSku())) {
            boolean checkExistVariantBySku = this.variantRepository.existsBySku(request.getSku());
            if(checkExistVariantBySku) throw new DuplicateResource("SKU already exists");
            productVariant.setSku(request.getSku());
        }
        String size = request.getSize();
        String color = request.getColor();
        boolean sizeChanged = CheckData.checkIsNotNull(size) && !size.equals(productVariant.getSize());
        boolean colorChanged = CheckData.checkIsNotNull(color) && !color.equals(productVariant.getColor());
        if(sizeChanged || colorChanged) {
            String sizeUpdate = !CheckData.checkIsNotNull(size) ? productVariant.getSize() : size;
            String colorUpdate = !CheckData.checkIsNotNull(color) ? productVariant.getColor() : color;
            boolean checkDuplicateVariant = this.variantRepository.existsByProductIdAndSizeAndColor(productVariant.getProduct().getId(), sizeUpdate, colorUpdate);
            if(checkDuplicateVariant) throw new DuplicateResource("Variant already exists");
            productVariant.setSize(sizeUpdate);
            productVariant.setColor(colorUpdate);
        }

        if(CheckData.checkIsNotNull(request.getImageUrl())) productVariant.setImageUrl(request.getImageUrl());
        if(CheckData.checkIsNotNull(request.getStock())) productVariant.setStock(request.getStock());
        if(CheckData.checkIsNotNull(request.getPrice())) productVariant.setPrice(request.getPrice());

        return ProductVariantResponse.fromEntity(this.variantRepository.save(productVariant));
    }

    /**
     * TODO: Xoá variant
     */
    public void deleteVariant(Long variantId) {
        ProductVariant productVariant = this.getProductVariantById(variantId);
        this.variantRepository.delete(productVariant);
    }

    private ProductVariant getProductVariantById(Long variantId) {
        return this.variantRepository.findById(variantId).orElseThrow(() -> new ResourceNotFoundException("Product Variant not found"));
    }
}
