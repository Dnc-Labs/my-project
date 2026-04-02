package com.ecommerce.api.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ShippingService {

    @Async
    public CompletableFuture<Integer> calculateFee(String orderId) {
        try {
            Thread.sleep(2000);
            return CompletableFuture.completedFuture(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
