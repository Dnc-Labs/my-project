package com.ecommerce.api.exception;

public class DuplicateResource extends RuntimeException{
    public DuplicateResource(String message) {
        super(message);
    }

    public DuplicateResource() {
        super("Duplicate Resource");
    }
}
