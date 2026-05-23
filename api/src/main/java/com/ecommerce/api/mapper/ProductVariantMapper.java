package com.ecommerce.api.mapper;

import com.ecommerce.api.dto.request.CreateProductVariantRequest;
import com.ecommerce.api.dto.request.UpdateProductVariantRequest;
import com.ecommerce.api.dto.response.ProductVariantResponse;

import com.ecommerce.api.entity.ProductVariant;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public interface ProductVariantMapper {
    @Mapping(target = "productId", source = "product.id")
    ProductVariantResponse fromEntity(ProductVariant productVariant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductVariant fromRequestDto(CreateProductVariantRequest createProductVariantRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "color", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateProductVariantRequest request, @MappingTarget ProductVariant productVariant);

}
