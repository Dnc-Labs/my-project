package com.ecommerce.api.dto.request;

import java.math.BigDecimal;

public class ProductRequestDto {
    private String name;
    private BigDecimal price;
    private String description;

    public ProductRequestDto() {}

    public ProductRequestDto(String name, BigDecimal price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }
}
