package com.ecommerce.api.mapper;

import com.ecommerce.api.dto.response.OrderDetailResponse;
import com.ecommerce.api.dto.response.OrderItemResponse;
import com.ecommerce.api.dto.response.OrderSummaryResponse;
import com.ecommerce.api.entity.Order;
import com.ecommerce.api.entity.OrderItem;
import com.ecommerce.api.entity.ProductVariant;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;

/**
 * OrderMapper — Entity → Response (read-only). KHÔNG map request→entity:
 * checkout dựng Order/OrderItem thủ công ở service (snapshot price, trừ stock).
 *
 * uses = UserMapper → tự map Order.customer (User) → OrderDetailResponse.customer (UserResponse).
 */
@Mapper(uses = {UserMapper.class})
public interface OrderMapper {

    OrderSummaryResponse toSummary(Order order);

    @Mapping(target = "items", source = "orderItems")
    OrderDetailResponse toDetail(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "variantId", source = "productVariant.id")
    @Mapping(target = "variantName", source = "productVariant", qualifiedByName = "orderVariantName")
    @Mapping(target = "subTotal", ignore = true)
    OrderItemResponse toItemResponse(OrderItem item);

    @Named("orderVariantName")
    default String orderVariantName(ProductVariant variant) {
        return variant == null ? null : variant.getSize() + " / " + variant.getColor();
    }

    @AfterMapping
    default void fillSubTotal(OrderItem item, @MappingTarget OrderItemResponse res) {
        res.setSubTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}
