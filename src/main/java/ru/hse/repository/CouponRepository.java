package ru.hse.repository;

import ru.hse.model.Coupon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for coupon data
 */
public class CouponRepository {
    private static final Logger logger = LoggerFactory.getLogger(CouponRepository.class);
    private final Map<String, Coupon> couponsByCode = new HashMap<>();
    
    /**
     * Get a coupon by code
     * 
     * @param code The coupon code
     * @return The coupon or null if not found
     */
    public Coupon findByCode(String code) {
        return couponsByCode.get(code.toUpperCase());
    }
    
    /**
     * Get all coupons
     * 
     * @return List of all coupons
     */
    public List<Coupon> findAll() {
        return new ArrayList<>(couponsByCode.values());
    }
    
    /**
     * Save a new coupon
     * 
     * @param coupon The coupon to save
     * @return The saved coupon
     */
    public Coupon save(Coupon coupon) {
        couponsByCode.put(coupon.getCode().toUpperCase(), coupon);
        logger.info("Saved coupon: {}", coupon);
        return coupon;
    }
    
    /**
     * Update an existing coupon
     * 
     * @param coupon The coupon to update
     * @return The updated coupon
     */
    public Coupon update(Coupon coupon) {
        couponsByCode.put(coupon.getCode().toUpperCase(), coupon);
        logger.debug("Updated coupon: {}", coupon);
        return coupon;
    }
    
    /**
     * Delete a coupon
     * 
     * @param code The coupon code to delete
     */
    public void delete(String code) {
        Coupon coupon = couponsByCode.remove(code.toUpperCase());
        if (coupon != null) {
            logger.info("Deleted coupon with code: {}", code);
        }
    }
    
    /**
     * Add a new coupon with the specified code and amount
     * 
     * @param code The coupon code
     * @param amount The amount to add to balance
     * @return The created coupon
     */
    public Coupon addCoupon(String code, BigDecimal amount) {
        Coupon coupon = new Coupon(code, amount);
        return save(coupon);
    }
}
