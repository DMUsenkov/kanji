package ru.hse.repository;

import ru.hse.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repository for user data
 */
public class UserRepository {
    private final Map<Long, User> usersById = new HashMap<>();
    private final Map<Long, User> usersByChatId = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    
    /**
     * Get a user by ID
     * 
     * @param id The user ID
     * @return The user or null if not found
     */
    public User findById(Long id) {
        return usersById.get(id);
    }
    
    /**
     * Get a user by Telegram chat ID
     * 
     * @param chatId The chat ID
     * @return The user or null if not found
     */
    public User findByChatId(Long chatId) {
        return usersByChatId.get(chatId);
    }
    
    /**
     * Get all users
     * 
     * @return List of all users
     */
    public List<User> findAll() {
        return new ArrayList<>(usersById.values());
    }
    
    /**
     * Save a new user
     * 
     * @param user The user to save
     * @return The saved user
     */
    public User save(User user) {
        usersById.put(user.getId(), user);
        usersByChatId.put(user.getChatId(), user);
        System.out.println("Saved user: " + user);
        return user;
    }
    
    /**
     * Update an existing user
     * 
     * @param user The user to update
     * @return The updated user
     */
    public User update(User user) {
        usersById.put(user.getId(), user);
        usersByChatId.put(user.getChatId(), user);
        System.out.println("Updated user: " + user);
        return user;
    }
    
    /**
     * Delete a user
     * 
     * @param id The user ID to delete
     */
    public void delete(Long id) {
        User user = usersById.remove(id);
        if (user != null) {
            usersByChatId.remove(user.getChatId());
            System.out.println("Deleted user with ID: " + id);
        }
    }
    
    /**
     * Generate next user ID
     * 
     * @return The next available ID
     */
    public Long nextId() {
        return idCounter.getAndIncrement();
    }
}
