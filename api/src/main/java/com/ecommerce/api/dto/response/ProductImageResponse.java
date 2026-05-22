package com.ecommerce.api.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO response cho ProductImage.
 * KHÔNG expose 'key' (định danh internal trên storage) ra ngoài —
 * client chỉ cần URL để truy cập, không cần biết file thật tên gì.
 */
@Getter
@Setter
public class ProductImageResponse {

    private Long id;
    private String url;
    private String originalName;
    private Boolean isPrimary;
}
