package ru.hse.repository;

import ru.hse.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Repository for order data
 */
public class OrderRepository {
    private final Map<Long, Order> ordersById = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    
    /**
     * Get an order by ID
     * 
     * @param id The order ID
     * @return The order or null if not found
     */
    public Order findById(Long id) {
        return ordersById.get(id);
    }
    
    /**
     * Get all orders
     * 
     * @return List of all orders
     */
    public List<Order> findAll() {
        return new ArrayList<>(ordersById.values());
    }
    
    /**
     * Get orders for a specific user
     * 
     * @param userId The user ID
     * @return List of user's orders
     */
    public List<Order> findByUserId(Long userId) {
        return ordersById.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
    
    /**
     * Save a new order
     * 
     * @param order The order to save
     * @return The saved order
     */
    public Order save(Order order) {
        ordersById.put(order.getId(), order);
        System.out.println("Saved order: " + order);
        return order;
    }
    
    /**
     * Generate next order ID
     * 
     * @return The next available ID
     */
    public Long nextId() {
        return idCounter.getAndIncrement();
    }
}
