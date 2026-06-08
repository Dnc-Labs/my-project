package com.ecommerce.api.mapper;

import com.ecommerce.api.dto.request.CreateProductRequest;
import com.ecommerce.api.dto.request.UpdateProductRequest;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.ProductImage;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(uses = {ProductImageMapper.class})
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "sellerName", source = "seller.fullName")
    @Mapping(target = "primaryImageUrl", source = "images", qualifiedByName = "extractPrimaryUrl")
    ProductResponse fromEntity(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "productVariants", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Product fromRequestDto(CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "productVariants", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateProductRequest request, @MappingTarget Product product);

    @Named("extractPrimaryUrl")
    default String extractPrimaryUrl(List<ProductImage> images) {
        if(images == null) return null;
        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .map(ProductImage::getUrl)
                .orElse(null);
    }
}
