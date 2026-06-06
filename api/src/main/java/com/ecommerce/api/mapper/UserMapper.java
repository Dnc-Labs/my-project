package com.ecommerce.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.ecommerce.api.dto.request.CreateUserRequest;
import com.ecommerce.api.dto.request.UpdateUserRequest;
import com.ecommerce.api.dto.response.UserResponse;
import com.ecommerce.api.entity.User;

@Mapper
public interface UserMapper {

    UserResponse fromEntity(User user);

    // password: ignore cố ý để service hash trước khi set, tránh plain password lọt qua mapper
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderList", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User fromRequestDto(CreateUserRequest request);

    // NullValuePropertyMappingStrategy.IGNORE: partial update — field null trong request không đè entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderList", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);
}
