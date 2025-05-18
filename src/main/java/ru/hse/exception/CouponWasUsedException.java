package ru.hse.exception;

/**
 * Exception thrown when a coupon is not found or is invalid
 */
public class CouponWasUsedException extends Exception {
    public CouponWasUsedException(String message) {
        super(message);
    }
}
