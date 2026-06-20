package com.ecommerce.api.mapper;

import com.ecommerce.api.dto.response.CartItemResponse;
import com.ecommerce.api.dto.response.CartResponse;
import com.ecommerce.api.entity.Cart;
import com.ecommerce.api.entity.CartItem;
import com.ecommerce.api.entity.ProductVariant;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;

/**
 * CartMapper — Entity → Response. KHÔNG có mapping request→entity:
 * CartItem được service tự dựng (cần findById Product/Variant), mapper không làm thay.
 *
 * Live price: unitPrice/subTotal/totalAmount tính lúc map, không lưu DB.
 */
@Mapper
public interface CartMapper {

    // ----- CartItem -> CartItemResponse -----
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "variantId", source = "productVariant.id")          // null-safe: variant null → null
    @Mapping(target = "variantName", source = "productVariant", qualifiedByName = "variantName")
    @Mapping(target = "unitPrice", ignore = true)   // set trong @AfterMapping (cần cả product + variant)
    @Mapping(target = "subTotal", ignore = true)    // set trong @AfterMapping (= unitPrice * quantity)
    CartItemResponse toItemResponse(CartItem item);

    // ----- Cart -> CartResponse -----
    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "items", source = "cartItems")   // MapStruct tự map List<CartItem> nhờ toItemResponse
    @Mapping(target = "totalAmount", ignore = true)    // set trong @AfterMapping (cộng dồn subTotal)
    @Mapping(target = "totalItems", ignore = true)     // set trong @AfterMapping (cộng dồn quantity)
    CartResponse toCartResponse(Cart cart);

    // ----- helpers (bạn code thân hàm) -----

    @Named("variantName")
    default String variantName(ProductVariant variant) {
        return variant == null ? null : variant.getSize() + " / " + variant.getColor();
    }

    @AfterMapping
    default void fillItemPrices(CartItem item, @MappingTarget CartItemResponse res) {
        BigDecimal unitPrice = (item.getProductVariant() != null) ? item.getProductVariant().getPrice() : item.getProduct().getPrice();
        BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        res.setUnitPrice(unitPrice);
        res.setSubTotal(subTotal);
    }

    @AfterMapping
    default void fillCartTotals(@MappingTarget CartResponse res) {
        BigDecimal totalAmount0 = res.getItems().stream().reduce(BigDecimal.ZERO, (a,b) -> a.add(b.getSubTotal()) , BigDecimal::add);
        BigDecimal totalAmount = res.getItems().stream()
                                                        .map(CartItemResponse::getSubTotal)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer quantity = res.getItems().stream().map(CartItemResponse::getQuantity).reduce(0, Integer::sum);
        res.setTotalAmount(totalAmount);
        res.setTotalItems(quantity);
    }
}
