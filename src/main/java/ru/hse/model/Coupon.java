package ru.hse.model;

import java.math.BigDecimal;

/**
 * Represents a coupon in the e-commerce system
 */
public class Coupon {
    private String code;
    private BigDecimal amount;
    public Coupon(String code, BigDecimal amount) {
        this.code = code;
        this.amount = amount;
    }
    
    public String getCode() {
        return code;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "code='" + code + '\'' +
                ", amount=" + amount +
                '}';
    }
}
