package com.ecommerce.api.mapper;

import com.ecommerce.api.dto.response.ProductImageResponse;

import com.ecommerce.api.entity.ProductImage;

import org.mapstruct.Mapper;

@Mapper
public interface ProductImageMapper {
    ProductImageResponse fromEntity(ProductImage image);
}
