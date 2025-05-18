package ru.hse.exception;

/**
 * Exception thrown when a user doesn't have enough balance for an operation
 */
public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
