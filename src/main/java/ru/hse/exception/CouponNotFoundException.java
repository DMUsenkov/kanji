package ru.hse.exception;

/**
 * Exception thrown when a coupon is not found or is invalid
 */
public class CouponNotFoundException extends Exception {
    public CouponNotFoundException(String message) {
        super(message);
    }
}
