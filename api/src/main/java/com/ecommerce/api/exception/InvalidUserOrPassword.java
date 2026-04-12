package com.ecommerce.api.exception;

public class InvalidUserOrPassword extends RuntimeException {
    public InvalidUserOrPassword() {
        super("Username or password is incorrect");
    }

    public InvalidUserOrPassword(String message) {
        super(message);
    }
}
