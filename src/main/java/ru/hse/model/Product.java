package ru.hse.model;

import java.math.BigDecimal;

/**
 * Represents a product in the e-commerce system
 */
public class Product {
    private Long id;
    private String name;
    private BigDecimal price;
    private int quantity;  // Available stock quantity
    
    public Product(Long id, String name, BigDecimal price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    /**
     * Reduces the product quantity by the specified amount
     * 
     * @param amount The amount to reduce by
     * @return true if there was sufficient stock, false otherwise
     */
    public boolean reduceQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Increases the product quantity by the specified amount
     * 
     * @param amount The amount to increase by
     */
    public void increaseQuantity(int amount) {
        quantity += amount;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
