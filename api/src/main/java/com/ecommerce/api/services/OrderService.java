package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.CreateOrderRequest;
import com.ecommerce.api.dto.response.OrderDetailResponse;
import com.ecommerce.api.dto.response.OrderSummaryResponse;
import com.ecommerce.api.dto.response.PageResponse;
import com.ecommerce.api.entity.Cart;
import com.ecommerce.api.entity.CartItem;
import com.ecommerce.api.entity.Order;
import com.ecommerce.api.entity.OrderItem;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.ProductVariant;
import com.ecommerce.api.enums.OrderStatus;
import com.ecommerce.api.enums.Role;
import com.ecommerce.api.exception.BusinessRuleException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.mapper.OrderMapper;
import com.ecommerce.api.repository.CartRepository;
import com.ecommerce.api.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderMapper orderMapper;
    private final EntityManager entityManager;

    // ============ CHECKOUT (trái tim của bài) ============

    /**
     * Đặt hàng từ giỏ hiện tại. TẤT CẢ trong 1 transaction → fail bất kỳ bước nào = rollback hết
     * (giỏ giữ nguyên, stock không bị trừ). Trừ stock dùng pessimistic lock (findByIdForUpdate)
     * → chống oversell, an toàn đa node (lock ở DB, không phải JVM).
     */
    @Transactional
    public OrderDetailResponse checkout(Long userId, CreateOrderRequest request) {

        Cart cart = cartRepository.findWithItemsByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        if(cart.getCartItems().isEmpty()) throw new BusinessRuleException("Cart is empty");

        Order order = new Order();
        order.setCustomer(cart.getUser());
        order.setNote(request.getNote());
        order.setShippingAddress(request.getShippingAddress());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for(CartItem ci : cart.getCartItems()) {
            BigDecimal unitPrice = lockAndDeductStock(ci);
            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity())));
            OrderItem item = new OrderItem();
            item.setQuantity(ci.getQuantity());
            item.setProduct(ci.getProduct());
            item.setProductVariant(ci.getProductVariant());
            item.setPrice(unitPrice);
            item.setOrder(order);
            order.getOrderItems().add(item);
        }

        order.setTotalAmount(totalAmount);
        Order saved = orderRepository.save(order);
        cart.getCartItems().clear();
        return orderMapper.toDetail(saved);
    }

    /**
     * Khóa product/variant (FOR UPDATE), check stock đủ, TRỪ stock, trả về unitPrice snapshot.
     * Tách riêng để checkout gọn. Lock giữ tới hết transaction checkout.
     */
    private BigDecimal lockAndDeductStock(CartItem cartItem) {

        ProductVariant variant = cartItem.getProductVariant();
        Product product = cartItem.getProduct();
        int quantity = cartItem.getQuantity();
        if(variant != null) {
            // refresh + PESSIMISTIC_WRITE: đọc lại state TƯƠI từ DB (đè state cũ pre-loaded) + khóa row.
            // Nếu chỉ findByIdForUpdate khi entity đã ở persistence context → khóa được nhưng state vẫn cũ → oversell.
            entityManager.refresh(variant, LockModeType.PESSIMISTIC_WRITE);
            if(variant.getStock() < quantity) throw new BusinessRuleException("Out of stock: " + variant.getSize() + " / " + variant.getColor());
            variant.setStock(variant.getStock() - quantity);
            return variant.getPrice();
        } else {
            entityManager.refresh(product, LockModeType.PESSIMISTIC_WRITE);
            if(product.getStock() < quantity) throw new BusinessRuleException("Out of stock: " + product.getName());
            product.setStock(product.getStock() - quantity);
            return product.getPrice();
        }
    }

    // ============ READ ============

    public PageResponse<OrderSummaryResponse> getMyOrders(Long userId, OrderStatus status, Pageable pageable) {
        Page<Order> page = status == null
                                        ? orderRepository.findByCustomerId(userId, pageable)
                                        : orderRepository.findByCustomerIdAndStatus( userId, status, pageable);
        return PageResponse.from(page.map(orderMapper::toSummary));
    }

    public OrderDetailResponse getOrderDetail(Long userId, Long orderId) {

        Order order = orderRepository.findWithItemById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        checkOwnership(order, userId);
        return orderMapper.toDetail(order);
    }

    // ============ ĐỔI TRẠNG THÁI (state machine + phân quyền + hoàn stock) ============

    /**
     * Đổi status theo state machine:
     *   PENDING ──confirm──> CONFIRMED ──ship──> SHIPPING ──deliver──> DELIVERED
     *   PENDING / CONFIRMED ──cancel──> CANCELLED   (sau SHIPPING không hủy được)
     *
     * Phân quyền:
     *   - CANCELLED: CUSTOMER (chủ đơn) khi status ∈ {PENDING, CONFIRMED}; hoặc SELLER/ADMIN
     *   - CONFIRMED/SHIPPING/DELIVERED: chỉ SELLER/ADMIN
     *
     * Khi → CANCELLED: HOÀN STOCK (cộng trả quantity vào product/variant, có lock).
     */
    @Transactional
    public OrderDetailResponse updateStatus(Long actorId, String actorRole, Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findWithItemById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        checkPermission(order, actorId, actorRole, newStatus);
        validateTransition(order.getStatus(), newStatus);
        if(newStatus == OrderStatus.CANCELLED) restock(order);
        order.setStatus(newStatus);
        return orderMapper.toDetail(order);
    }

    // ============ helpers ============

    private void validateTransition(OrderStatus current, OrderStatus next) {

        boolean ok = switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.SHIPPING || next == OrderStatus.CANCELLED;
            case  SHIPPING -> next == OrderStatus.DELIVERED;
            default -> false;
        };
        if(!ok) throw new BusinessRuleException("Invalid status transition: " + current + " -> " + next);
    }

    private void checkPermission(Order order, Long actorId, String actorRole, OrderStatus newStatus) {
        boolean isStaff = Role.ADMIN.toString().equals(actorRole) || Role.SELLER.toString().equals(actorRole);
        if(newStatus.equals(OrderStatus.CANCELLED)) {
            boolean isOwner = order.getCustomer().getId().equals(actorId);
            if(!isOwner && !isStaff) throw new BusinessRuleException("Not allowed to cancel this order");
        } else if(!isStaff) {
            throw new BusinessRuleException("Only seller/admin can advance order status");
        }
    }

    private void restock(Order order) {
       for(OrderItem ot : order.getOrderItems()) {
           Product product = ot.getProduct();
           ProductVariant variant = ot.getProductVariant();
           int quantity = ot.getQuantity();
           if(variant == null) {
               entityManager.refresh(product, LockModeType.PESSIMISTIC_WRITE);
               product.setStock(product.getStock() + quantity);
           }
           else {
               entityManager.refresh(variant, LockModeType.PESSIMISTIC_WRITE);
               variant.setStock(variant.getStock() + quantity);
           }
       }
    }

    private void checkOwnership(Order order, Long userId) {
        if(!order.getCustomer().getId().equals(userId)) throw new ResourceNotFoundException("Order not found");
    }
}
