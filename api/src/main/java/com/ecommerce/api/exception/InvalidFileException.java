package com.ecommerce.api.exception;

public class InvalidFileException extends RuntimeException {
    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException() {
        super("Unsupported file type");
    }
}
