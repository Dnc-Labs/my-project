package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.request.AddToCartRequest;
import com.ecommerce.api.dto.request.UpdateCartItemRequest;
import com.ecommerce.api.dto.response.BaseResponse;
import com.ecommerce.api.dto.response.CartResponse;
import com.ecommerce.api.security.CustomUserDetails;
import com.ecommerce.api.services.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<BaseResponse<CartResponse>> getMyCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(BaseResponse.success(cartService.getMyCart(userDetails.getId())));
    }

    @PostMapping("/items")
    public ResponseEntity<BaseResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
            CartResponse cart = cartService.addToCart(userDetails.getId(), request);
        return ResponseEntity.ok(BaseResponse.success(cart));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<BaseResponse<CartResponse>> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse cart = cartService.updateItem(userDetails.getId(), itemId, request);
        return ResponseEntity.ok(BaseResponse.success(cart));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<BaseResponse<?>> removeItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
            cartService.removeItem(userDetails.getId(), itemId);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<?>> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.clearCart(userDetails.getId());
        return ResponseEntity.ok(BaseResponse.success(null));
    }
}
