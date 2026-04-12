package com.ecommerce.api.exception;

public class InvalidToken extends RuntimeException{
    public InvalidToken() {
        super("Invalid Token");
    }
}
