package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    @NotBlank
    private String sku;

    @Positive
    @NotNull
    private BigDecimal price;

    private String description;

    @NotNull
    @PositiveOrZero
    private Integer stock = 0;

    @NotNull
    private Long categoryId;
}
