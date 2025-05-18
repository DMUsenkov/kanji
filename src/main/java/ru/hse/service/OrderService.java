package ru.hse.service;

import ru.hse.exception.InsufficientBalanceException;
import ru.hse.exception.InsufficientStockException;
import ru.hse.model.CartItem;
import ru.hse.model.Order;
import ru.hse.model.Product;
import ru.hse.model.User;
import ru.hse.repository.OrderRepository;
import ru.hse.repository.ProductRepository;
import ru.hse.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for order operations
 */
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    public OrderService(OrderRepository orderRepository, 
                         ProductRepository productRepository,
                         UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Process an order for a user
     * 
     * @param user The user placing the order
     * @return The created order
     * @throws InsufficientBalanceException if the user doesn't have enough balance
     * @throws InsufficientStockException if there's not enough stock for any product
     */
    public Order createOrder(User user)
            throws InsufficientBalanceException, InsufficientStockException {
        if (user.getCart().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order with empty cart");
        }
        
        // Calculate total order amount
        BigDecimal total = user.getCartTotal();
        
        // Check if user has enough balance
        if (user.getBalance().compareTo(total) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Required: $" + total + 
                    ", Available: $" + user.getBalance());
        }
        
        // Check if all products have enough stock
        for (CartItem item : user.getCart()) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            
            if (product.getQuantity() < quantity) {
                throw new InsufficientStockException(
                        "Not enough stock for " + product.getName() + 
                        ". Available: " + product.getQuantity() + 
                        ", Requested: " + quantity);
            }
        }
        
        // All checks passed, create the order
        
        // Create a deep copy of cart items to preserve order details
        List<CartItem> orderItems = user.getCart().stream()
                .map(item -> new CartItem(item.getProduct(), item.getQuantity()))
                .collect(Collectors.toList());
        
        // Create order
        Order order = new Order(orderRepository.nextId(), user.getId(), orderItems, total);
        
        // Update product stock
        for (CartItem item : user.getCart()) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            
            product.reduceQuantity(quantity);
            productRepository.update(product);
        }
        
        // Deduct amount from user balance
        user.subtractFromBalance(total);
        
        // Clear user's cart
        user.clearCart();
        
        // Save everything
        userRepository.update(user);
        orderRepository.save(order);
        
        System.out.println("Created order "+order.getId()+" for user "+user.getId()+" with total $"+total);
        
        return order;
    }
    
    /**
     * Get all orders for a user
     * 
     * @param user The user
     * @return List of orders
     */
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserId(user.getId());
    }
    
    /**
     * Get an order by its ID
     * 
     * @param orderId The order ID
     * @return The order or null if not found
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
}
