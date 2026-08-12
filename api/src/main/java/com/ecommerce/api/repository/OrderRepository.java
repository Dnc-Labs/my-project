package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Order;
import com.ecommerce.api.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable page);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product", "orderItems.productVariant"})
    Optional<Order> findWithItemById(Long orderId);

    /**
     * Verified purchase — user đã mua product này và đơn đã ở trạng thái mong muốn chưa.
     * Không có bảng nối thẳng user ↔ product nên phải đi qua orders + order_items.
     * <p>
     * EXISTS thay vì COUNT(*) > 0: dừng ngay ở dòng khớp đầu tiên thay vì đếm hết
     * (khách mua 50 lần thì khác biệt là 50 dòng so với 1).
     * <p>
     * status nhận String chứ không phải enum: native query không có metadata mapping nên
     * Hibernate phải tự đoán cách bind enum (dễ ra ordinal) rồi so sánh sai với cột ENUM
     * của MySQL. Caller truyền OrderStatus.DELIVERED.name() để vẫn giữ kiểm tra của compiler.
     */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM orders o
                INNER JOIN order_items oi on oi.order_id = o.id
                WHERE
                o.customer_id= :userId
                AND oi.product_id= :productId
                AND o.status= :status )
            """, nativeQuery = true)
    boolean verifiedPurchase(@Param("userId") Long userId,
                             @Param("productId") Long productId,
                             @Param("status") String status);
}
