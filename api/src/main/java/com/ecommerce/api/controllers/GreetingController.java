package com.ecommerce.api.controllers;


import com.ecommerce.api.services.GreetingService;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/greeting")
public class GreetingController {

     private final GreetingService greetingService;

     GreetingController(GreetingService greetingService) {
         this.greetingService = greetingService;
     }

    @GetMapping
    ResponseEntity<String> getGreeting(@RequestParam(required = false) String name) {
        String result =  this.greetingService.handleGetGreeting(name);
        return ResponseEntity.ok(result);
    }
}
