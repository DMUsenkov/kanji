package ru.hse.exception;

/**
 * Exception thrown when a product is not found
 */
public class ProductNotFoundException extends Exception {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
