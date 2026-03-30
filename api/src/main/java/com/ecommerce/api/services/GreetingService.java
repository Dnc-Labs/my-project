package com.ecommerce.api.services;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    public String handleGetGreeting(@Nullable String name) {
        if(name == null || name.isEmpty()) return "Hello, Spring Boot";
        return "Hello, " + name;
    }
}
