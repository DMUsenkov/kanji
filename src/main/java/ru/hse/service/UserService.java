package ru.hse.service;

import ru.hse.model.Product;
import ru.hse.model.SessionState;
import ru.hse.model.User;
import ru.hse.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashSet;

/**
 * Service class for user operations
 */
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Get a user by their chat ID
     * 
     * @param chatId The Telegram chat ID
     * @return The user or null if not found
     */
    public User getUserByChatId(Long chatId) {
        return userRepository.findByChatId(chatId);
    }
    
    /**
     * Create a new user
     * 
     * @param chatId The Telegram chat ID
     * @param name The user's name
     * @return The newly created user
     */
    public User createUser(Long chatId, String name) {
        User existingUser = userRepository.findByChatId(chatId);
        
        if (existingUser != null) {
            System.out.println("User already exists with chatId: "+ chatId);
            return existingUser;
        }
        
        User newUser = new User(userRepository.nextId(), chatId, name, new HashSet<>());
        
        userRepository.save(newUser);
        System.out.println("Created new user: "+newUser);
        
        return newUser;
    }
    
    /**
     * Add funds to a user's balance
     * 
     * @param user The user
     * @param amount The amount to add
     */
    public void addFunds(User user, BigDecimal amount) {
        user.addToBalance(amount);
        userRepository.update(user);
        System.out.println("Added "+amount+" funds to user "+user.getId()+", new balance: "+user.getBalance());
    }
    
    /**
     * Deduct funds from a user's balance
     * 
     * @param user The user
     * @param amount The amount to deduct
     * @return true if successful, false if insufficient funds
     */
    public boolean deductFunds(User user, BigDecimal amount) {
        if (user.getBalance().compareTo(amount) < 0) {
            System.out.println("Insufficient funds for user "+user.getId()+": has "+user.getBalance()+", needs "+amount);
            return false;
        }
        
        user.subtractFromBalance(amount);
        userRepository.update(user);
        System.out.println("Deducted "+amount+" funds from user "+user.getId()+", new balance: "+user.getBalance());
        return true;
    }
    
    /**
     * Add a product to the user's cart
     * 
     * @param user The user
     * @param product The product to add
     * @param quantity The quantity to add
     */
    public void addToCart(User user, Product product, int quantity) {
        user.addToCart(product, quantity);
        userRepository.update(user);
        System.out.println("Added "+quantity+" of "+product.getName()+" to cart for user "+user.getId());
    }
    
    /**
     * Clear the user's cart
     * 
     * @param user The user
     */
    public void clearCart(User user) {
        user.clearCart();
        userRepository.update(user);
        System.out.println("Cleared cart for user "+user.getId());
    }
}
