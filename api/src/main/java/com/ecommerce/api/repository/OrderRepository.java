package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Order;
import com.ecommerce.api.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable page);
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product", "orderItems.productVariant"})
    Optional<Order> findWithItemById(Long orderId);
}
