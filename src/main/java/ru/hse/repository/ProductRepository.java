package ru.hse.repository;

import ru.hse.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repository for product data
 */
public class ProductRepository {
    private final Map<Long, Product> productsById = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    
    /**
     * Get a product by ID
     * 
     * @param id The product ID
     * @return The product or null if not found
     */
    public Product findById(Long id) {
        return productsById.get(id);
    }
    
    /**
     * Get all products
     * 
     * @return List of all products
     */
    public List<Product> findAll() {
        return new ArrayList<>(productsById.values());
    }
    
    /**
     * Save a new product
     * 
     * @param product The product to save
     * @return The saved product
     */
    public Product save(Product product) {
        productsById.put(product.getId(), product);
        System.out.println("Saved product: "+product);
        return product;
    }
    
    /**
     * Update an existing product
     * 
     * @param product The product to update
     * @return The updated product
     */
    public Product update(Product product) {
        productsById.put(product.getId(), product);
        System.out.println("Updated product: "+product);
        return product;
    }
    
    /**
     * Delete a product
     * 
     * @param id The product ID to delete
     */
    public void delete(Long id) {
        Product product = productsById.remove(id);
        if (product != null) {
            System.out.println("Deleted product with ID: " + id);
        }
    }
    
    /**
     * Generate next product ID
     * 
     * @return The next available ID
     */
    public Long nextId() {
        return idCounter.getAndIncrement();
    }
}
