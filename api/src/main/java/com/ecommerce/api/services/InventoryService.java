package com.ecommerce.api.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class InventoryService {

    @Async
    public CompletableFuture<Boolean> checkStock(String orderId) {
        try {
            Thread.sleep(1000);
            return CompletableFuture.completedFuture(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
