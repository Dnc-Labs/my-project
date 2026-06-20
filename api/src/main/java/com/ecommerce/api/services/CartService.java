package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.AddToCartRequest;
import com.ecommerce.api.dto.request.UpdateCartItemRequest;
import com.ecommerce.api.dto.response.CartResponse;
import com.ecommerce.api.entity.Cart;
import com.ecommerce.api.entity.CartItem;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.ProductVariant;
import com.ecommerce.api.enums.ProductStatus;
import com.ecommerce.api.exception.BusinessRuleException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.mapper.CartMapper;
import com.ecommerce.api.repository.CartItemRepository;
import com.ecommerce.api.repository.CartRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.ProductVariantRepository;
import com.ecommerce.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartResponse getMyCart(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId).orElseGet(() -> getOrCreateCart(userId));
        return cartMapper.toCartResponse(cart);
    }


    /**
     * Get-or-create cart cho user. Private helper → chạy chung tx của caller
     * (đúng ý muốn: tạo cart fail thì rollback theo). KHÔNG cần @Transactional riêng,
     * và nếu có cũng vô hiệu vì self-invocation không đi qua proxy.
     */
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
           Cart cart = new Cart();
           cart.setUser(userRepository.getReferenceById(userId));
           return cartRepository.save(cart);
        });
    }

    // ============ WRITE ============

    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findByIdAndStatus(request.getProductId(), ProductStatus.ACTIVE).orElseThrow(() -> new ResourceNotFoundException("Product not found or not available"));
        ProductVariant variant = (request.getVariantId() != null ) ? variantRepository.findById(request.getVariantId()).orElseThrow(() -> new ResourceNotFoundException("Product variant not found")) : null;
        if(variant != null && !variant.getProduct().getId().equals(request.getProductId())) throw new BusinessRuleException("Variant does not belong to this product");
        int availableStock = variant == null ? product.getStock() : variant.getStock();
        CartItem existItem = cartItemRepository.findExisting(cart.getId(), request.getProductId(), request.getVariantId()).orElse(null);
        if(existItem != null) {
            checkStock(existItem.getQuantity() + request.getQuantity() , availableStock);
            existItem.setQuantity(existItem.getQuantity() + request.getQuantity());
        }
        else {
            checkStock(request.getQuantity() , availableStock);
            CartItem newItem = new CartItem();
            newItem.setQuantity(request.getQuantity());
            newItem.setProduct(product);
            newItem.setProductVariant(variant);
            newItem.setCart(cart);
            cart.getCartItems().add(newItem);
        }
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        CartItem item = findItemOfUser(userId, itemId);
        int availableStock = item.getProductVariant() == null ? item.getProduct().getStock() : item.getProductVariant().getStock();
        checkStock(request.getQuantity(), availableStock);
        item.setQuantity(request.getQuantity());
        return cartMapper.toCartResponse(item.getCart());
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        CartItem item = findItemOfUser(userId, itemId);
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getCartItems().clear();
    }

    private CartItem findItemOfUser(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if(!item.getCart().getUser().getId().equals(userId)) throw new ResourceNotFoundException("Cart item not found");
        return item;
    }

    private void checkStock(int requestedQty, int availableStock) {
        if(requestedQty > availableStock) throw new BusinessRuleException("Requested quantity exceeds available stock");
    }
}
