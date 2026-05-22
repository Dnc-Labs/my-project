package com.ecommerce.api.dto.request;

import com.ecommerce.api.enums.ProductStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class UpdateProductRequest {
    private String name;
    private String slug;
    private String sku;

    @Positive
    private BigDecimal price;
    private String description;

    @PositiveOrZero
    private Integer stock = 0;
    private ProductStatus status;
    private Long categoryId;

}
