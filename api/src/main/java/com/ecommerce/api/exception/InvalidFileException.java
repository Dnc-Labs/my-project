package com.ecommerce.api.exception;

public class InvalidFileException extends RuntimeException {
    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public InvalidFileException() {
        super("Unsupported file type");
    }
}
