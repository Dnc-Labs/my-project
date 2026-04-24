package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.CreateProductRequest;
import com.ecommerce.api.dto.request.UpdateProductRequest;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.entity.Category;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.enums.ProductStatus;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.CategoryRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.utilities.CheckData;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Tạo product — seller lấy từ SecurityContext (user đang login),
     * không lấy từ request để tránh gian lận (seller A giả danh seller B).
     */
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email =  authentication.getName();
        User user = this.userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        product.setSeller(user);

        String slug = request.getSlug();
        boolean checkExistsProductBySlug = this.productRepository.existsBySlug(slug);
        if(checkExistsProductBySlug) throw new RuntimeException("Slug already exists");
        product.setSlug(slug);

        String sku = request.getSku();
        boolean checkExistsProductBySKU = this.productRepository.existsBySku(sku);
        if(checkExistsProductBySKU) throw new RuntimeException("SKU already exists");
        product.setSku(sku);


        Long categoryId = request.getCategoryId();

        if(CheckData.checkIsNotNull(categoryId)) {
            Category category = this.categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setCreatedAt(LocalDateTime.now());
        Product savedProduct = this.productRepository.save(product);
        return ProductResponse.fromEntity(savedProduct);
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products =  this.productRepository.findAll();
        return products.stream().map(ProductResponse::fromEntity).toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = this.productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return ProductResponse.fromEntity(product);
    }

    /**
     * Cập nhật product. Ownership check đã xử lý ở @PreAuthorize controller —
     * service chỉ tập trung vào business logic.
     */
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = this.productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        String slug = request.getSlug();
        if(CheckData.checkIsNotNull(slug) && !product.getSlug().equals(slug)) {
            boolean checkExistsBySlug = this.productRepository.existsBySlug(slug);
            if(checkExistsBySlug) throw new RuntimeException("Slug already exists");
            product.setSlug(slug);
        }

        String sku = request.getSku();
        if(CheckData.checkIsNotNull(sku) && !product.getSku().equals(sku)) {
            boolean checkExistsBySku = this.productRepository.existsBySku(sku);
            if(checkExistsBySku) throw new RuntimeException("SKU already exists");
            product.setSku(sku);
        }

        Long categoryId = request.getCategoryId();
        if(CheckData.checkIsNotNull(categoryId) && !product.getCategory().getId().equals(categoryId)) {
            Category category = this.categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        BigDecimal price = request.getPrice();
        String name = request.getName();
        String description = request.getDescription();
        Integer stock = request.getStock();
        String imageUrl = request.getImageUrl();
        ProductStatus productStatus = request.getStatus();

        if(CheckData.checkIsNotNull(price)) product.setPrice(price);
        if(CheckData.checkIsNotNull(name)) product.setName(name);
        if(CheckData.checkIsNotNull(description)) product.setDescription(description);
        if(CheckData.checkIsNotNull(stock)) product.setStock(stock);
        if(CheckData.checkIsNotNull(imageUrl)) product.setImageUrl(imageUrl);
        if(CheckData.checkIsNotNull(productStatus)) product.setStatus(productStatus);
        product.setUpdatedAt(LocalDateTime.now());

        Product productUpdated = this.productRepository.save(product);
        return ProductResponse.fromEntity(productUpdated);
    }

    public void deleteProduct(Long id) {
        Product product = this.productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        this.productRepository.deleteById(id);
    }
}
