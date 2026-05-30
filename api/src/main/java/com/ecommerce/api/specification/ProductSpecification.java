package com.ecommerce.api.specification;

import com.ecommerce.api.entity.Product;
import com.ecommerce.api.enums.ProductStatus;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

/**
 * Factory các mảnh Specification<Product> để compose query động.
 * Mỗi static method = 1 mệnh đề WHERE độc lập, tái sử dụng được.
 * <p>
 * Lưu ý field name dạng String (root.get("price")) → typo sẽ lỗi RUNTIME,
 * không phải compile time. (Nâng cao: JPA Metamodel / QueryDSL để type-safe.)
 */
public class ProductSpecification {

    // utility class chỉ chứa static method — private constructor chặn new
    private ProductSpecification() {
    }

    public static Specification<Product> hasKeyword(String keyword) {
        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        );
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        // root.get("category").get("id"): đi qua quan hệ ManyToOne → Hibernate tự JOIN categories
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> priceGoe(BigDecimal minPrice) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLoe(BigDecimal maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
