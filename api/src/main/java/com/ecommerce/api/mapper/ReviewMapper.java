package com.ecommerce.api.mapper;

import com.ecommerce.api.dto.request.ReviewRequest;
import com.ecommerce.api.dto.response.ReviewResponse;
import com.ecommerce.api.entity.Review;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public interface ReviewMapper {

    // MapStruct KHÔNG tự match "userId" ↔ user.id — phải khai tường minh từng field phẳng hoá
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source="user.fullName")
    @Mapping(target = "productId", source = "product.id")
    ReviewResponse fromEntity(Review review);

    // unmappedTargetPolicy = ERROR (khai global ở pom) → thiếu 1 ignore là compile error.
    // product/user cố ý ignore: service tự set để kiểm soát nguồn (product từ DB, user từ token).
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Review fromRequestDto(ReviewRequest request);
}
