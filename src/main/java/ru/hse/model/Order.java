package ru.hse.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Represents an order in the e-commerce system
 */
public class Order {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private Long id;
    private Long userId;
    private List<CartItem> items;
    private BigDecimal total;
    private LocalDateTime createdAt;
    
    public Order(Long id, Long userId, List<CartItem> items, BigDecimal total) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.total = total;
        this.createdAt = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public List<CartItem> getItems() {
        return items;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public LocalDateTime getCreatedAtRaw() {
        return createdAt;
    }
    
    public String getCreatedAt() {
        return createdAt.format(DATE_FORMATTER);
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", items=" + items.size() +
                ", total=" + total +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
