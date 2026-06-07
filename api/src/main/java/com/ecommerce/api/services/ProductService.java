package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.CreateProductRequest;
import com.ecommerce.api.dto.request.ProductFilterRequest;
import com.ecommerce.api.dto.request.UpdateProductRequest;
import com.ecommerce.api.dto.response.PageResponse;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.entity.Category;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.exception.DuplicateResource;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.mapper.ProductMapper;
import com.ecommerce.api.repository.CategoryRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.specification.ProductSpecification;
import com.ecommerce.api.utilities.CheckData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    /**
     * Tạo product — seller lấy từ SecurityContext (user đang login),
     * không lấy từ request để tránh gian lận (seller A giả danh seller B).
     */
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Created new product : {}", request.getSlug());
        Product product = productMapper.fromRequestDto(request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email =  authentication.getName();
        User user = this.userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        product.setSeller(user);

        String slug = request.getSlug();
        boolean checkExistsProductBySlug = this.productRepository.existsBySlug(slug);
        if(checkExistsProductBySlug) throw new DuplicateResource("Slug already exists");
        product.setSlug(slug);

        String sku = request.getSku();
        boolean checkExistsProductBySKU = this.productRepository.existsBySku(sku);
        if(checkExistsProductBySKU) throw new DuplicateResource("SKU already exists");
        product.setSku(sku);


        Long categoryId = request.getCategoryId();

        if(CheckData.checkIsNotNull(categoryId)) {
            Category category = this.categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        Product savedProduct = this.productRepository.save(product);
        log.info("Product created successfully: id={}", savedProduct.getId());
        return productMapper.fromEntity(savedProduct);
    }

    public PageResponse<ProductResponse> getAllProducts(ProductFilterRequest filter, Pageable pageable) {
        // Build Specification động: chỉ filter nào user thực sự truyền mới vào WHERE,
        // giúp DB optimizer chọn index tốt (khác @Query "IS NULL OR" làm optimizer mù).
        // Specification immutable → BẮT BUỘC gán lại spec = spec.and(...).
        Specification<Product> spec = Specification.unrestricted();
        if (CheckData.checkIsNotNull(filter.getKeyword()))    spec = spec.and(ProductSpecification.hasKeyword(filter.getKeyword()));
        if (CheckData.checkIsNotNull(filter.getCategoryId())) spec = spec.and(ProductSpecification.hasCategory(filter.getCategoryId()));
        if (CheckData.checkIsNotNull(filter.getMinPrice()))   spec = spec.and(ProductSpecification.priceGoe(filter.getMinPrice()));
        if (CheckData.checkIsNotNull(filter.getMaxPrice()))   spec = spec.and(ProductSpecification.priceLoe(filter.getMaxPrice()));
        if (CheckData.checkIsNotNull(filter.getStatus()))     spec = spec.and(ProductSpecification.hasStatus(filter.getStatus()));

        Page<ProductResponse> products = productRepository.findAll(spec, pageable).map(productMapper::fromEntity);
        return PageResponse.from(products);
    }

    public ProductResponse getProductById(Long id) {
        Product product = this.productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.fromEntity(product);
    }

    /**
     * Cập nhật product. Ownership check đã xử lý ở @PreAuthorize controller —
     * service chỉ tập trung vào business logic.
     */
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Update product : {}", id);
        Product product = this.productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        productMapper.updateEntity(request, product);
        String slug = request.getSlug();
        if(CheckData.checkIsNotNull(slug) && !product.getSlug().equals(slug)) {
            boolean checkExistsBySlug = this.productRepository.existsBySlug(slug);
            if(checkExistsBySlug) throw new DuplicateResource("Slug already exists");
            product.setSlug(slug);
        }

        String sku = request.getSku();
        if(CheckData.checkIsNotNull(sku) && !product.getSku().equals(sku)) {
            boolean checkExistsBySku = this.productRepository.existsBySku(sku);
            if(checkExistsBySku) throw new DuplicateResource("SKU already exists");
            product.setSku(sku);
        }

        Long categoryId = request.getCategoryId();
        if(CheckData.checkIsNotNull(categoryId) &&
                (product.getCategory() == null || !product.getCategory().getId().equals(categoryId))) {
            Category category = this.categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }
        Product updated = this.productRepository.save(product);
        log.info("Update successfully product : {}", id);

        return productMapper.fromEntity(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Delete product : {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        productRepository.delete(product);
        log.info("Deleted successfully product : {}", id);

    }
}
