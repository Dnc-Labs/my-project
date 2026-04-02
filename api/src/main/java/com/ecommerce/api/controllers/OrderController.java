package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.request.OrderRequestDto;
import com.ecommerce.api.dto.response.OrderDetailResponse;
import com.ecommerce.api.services.EmailService;
import com.ecommerce.api.services.InventoryService;
import com.ecommerce.api.services.OrderService;
import com.ecommerce.api.services.ShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final EmailService emailService;
    private final OrderService orderService;
    private final ShippingService shippingService;
    private final InventoryService inventoryService;

    public OrderController(EmailService emailService, OrderService orderService, ShippingService shippingService, InventoryService inventoryService) {
        this.emailService = emailService;
        this.orderService = orderService;
        this.shippingService = shippingService;
        this.inventoryService = inventoryService;
    }



    @PostMapping
    public ResponseEntity<String> handleCreateOrder(@RequestBody OrderRequestDto orderRequestDto){
        String orderId = UUID.randomUUID().toString();
        logger.info("Đơn hàng đã tạo - Thread: {}", Thread.currentThread().getName());
        this.emailService.sendOrderConfirmation(orderRequestDto.getEmail(), orderId);
        return ResponseEntity.ok("Đơn hàng đã tạo " + orderId);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable String id) {
        long start = System.currentTimeMillis();

        CompletableFuture<String> orderFuture = orderService.getOrder(id);
        CompletableFuture<Integer> feeFuture = shippingService.calculateFee(id);
        CompletableFuture<Boolean> stockFuture = inventoryService.checkStock(id);
        CompletableFuture.allOf(orderFuture, feeFuture, stockFuture).join();

        String order = orderFuture.join();
        Integer fee = feeFuture.join();
        Boolean inStock = stockFuture.join();

        long duration = System.currentTimeMillis() - start;
        logger.info("Tổng thời gian: {} ms (song song thay vì ~4000ms tuần tự)", duration);

        return ResponseEntity.ok(new OrderDetailResponse(order, fee, inStock));
    }
}
