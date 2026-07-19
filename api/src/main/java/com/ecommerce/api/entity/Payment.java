package com.ecommerce.api.entity;

import com.ecommerce.api.enums.PaymentProvider;
import com.ecommerce.api.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment — một lần thử thanh toán cho một Order.
 * Tách khỏi Order vì một đơn có thể thử thanh toán nhiều lần (fail → retry),
 * và vòng đời tiền (PENDING/SUCCESS/FAILED) độc lập với vòng đời giao hàng.
 */
@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(unique = true, nullable = false)
    private String txnRef;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider = PaymentProvider.VNPAY;

    private String transactionNo;

    private LocalDateTime paidAt;

    @Column(columnDefinition = "TEXT")
    private String rawResponse;
}
