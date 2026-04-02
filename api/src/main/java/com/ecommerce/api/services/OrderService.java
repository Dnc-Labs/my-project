package com.ecommerce.api.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderService {

    @Async
    public CompletableFuture<String> getOrder(String orderId) {
        try {
            Thread.sleep(1000);
            return CompletableFuture.completedFuture("Order " + orderId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
