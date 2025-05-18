package ru.hse.exception;

/**
 * Exception thrown when there's not enough stock for a product
 */
public class InsufficientStockException extends Exception {
    public InsufficientStockException(String message) {
        super(message);
    }
}
