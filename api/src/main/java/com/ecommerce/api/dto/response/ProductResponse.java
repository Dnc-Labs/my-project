package com.ecommerce.api.dto.response;

import com.ecommerce.api.enums.ProductStatus;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * DTO response cho Product.
 * Category và seller được flatten (id + name) để tránh infinite loop khi serialize JSON.
 */

@Getter
@Setter
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String sku;
    private BigDecimal price;
    private String description;
    private Integer stock;
    private ProductStatus status;
    private Long categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String primaryImageUrl;          // URL ảnh isPrimary, null nếu chưa upload
    private List<ProductImageResponse> images = Collections.emptyList();
}
