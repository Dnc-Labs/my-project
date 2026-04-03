package com.ecommerce.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // - name (String — ví dụ: "Đỏ - Size M", "Xanh - Size L")
    // - sku (String, unique — mã định danh variant)
    // - price (BigDecimal — giá riêng của variant, có thể khác giá gốc Product)
    // - stock (Integer — tồn kho của variant này)
    private String name;
    @Column(unique = true)
    private String sku;
    private BigDecimal price;
    private Integer stock;

    // - product: nhiều Variant thuộc 1 Product (@ManyToOne)
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
