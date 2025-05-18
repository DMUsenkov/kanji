package ru.hse.service;

import ru.hse.exception.InsufficientStockException;
import ru.hse.exception.ProductNotFoundException;
import ru.hse.model.Product;
import ru.hse.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service class for product operations
 */
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * Get all available products
     *
     * @return
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    /**
     * Get a product by its ID
     * 
     * @param id The product ID
     * @return The product or null if not found
     */
    public Product getProductById(Long id) throws ProductNotFoundException {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new ProductNotFoundException("Product with ID " + id + " not found");
        }
        return product;
    }
    
    /**
     * Check if a product has sufficient stock
     * 
     * @param productId The product ID
     * @param quantity The quantity to check
     * @return true if there's enough stock
     * @throws ProductNotFoundException if the product doesn't exist
     * @throws InsufficientStockException if there's not enough stock
     */
    public boolean hasStock(Long productId, int quantity) 
            throws ProductNotFoundException, InsufficientStockException {
        Product product = getProductById(productId);
        
        if (product.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Not enough stock for " + product.getName() + 
                    ". Available: " + product.getQuantity() +
                    ", Requested: " + quantity);
        }
        
        return true;
    }
    
    /**
     * Update product stock
     * 
     * @param productId The product ID
     * @param quantity The quantity to reduce by (negative to increase)
     * @throws ProductNotFoundException if the product doesn't exist
     * @throws InsufficientStockException if there's not enough stock
     */
    public void updateStock(Long productId, int quantity) 
            throws ProductNotFoundException, InsufficientStockException {
        Product product = getProductById(productId);
        
        if (quantity > 0 && product.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Not enough stock for " + product.getName() + 
                    ". Available: " + product.getQuantity() +
                    ", Requested: " + quantity);
        }
        
        if (quantity > 0) {
            product.reduceQuantity(quantity);
        } else {
            product.increaseQuantity(-quantity);
        }
        
        productRepository.update(product);
        System.out.println("Updated stock for product "+product.getId()+": new quantity "+product.getQuantity());
    }
}
