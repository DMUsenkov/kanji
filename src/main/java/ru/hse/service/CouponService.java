package ru.hse.service;

import ru.hse.exception.CouponNotFoundException;
import ru.hse.exception.CouponWasUsedException;
import ru.hse.model.Coupon;
import ru.hse.model.User;
import ru.hse.repository.CouponRepository;
import ru.hse.repository.UserRepository;

import java.math.BigDecimal;

/**
 * Service class for coupon operations
 */
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    
    public CouponService(CouponRepository couponRepository, UserRepository userRepository) {
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Apply a coupon to add funds to a user's balance
     * 
     * @param user The user
     * @param couponCode The coupon code
     * @return The amount added to the user's balance
     * @throws CouponNotFoundException if the coupon doesn't exist or is already used
     */
    public BigDecimal applyCoupon(User user, String couponCode) throws CouponNotFoundException, CouponWasUsedException {
        Coupon coupon = couponRepository.findByCode(couponCode);
        
        if (coupon == null) {
            throw new CouponNotFoundException("Coupon code " + couponCode + " not found");
        }
        
        if (user.checkCouponUsed(coupon)) {
            throw new CouponWasUsedException("Coupon code " + couponCode + " has already been used");
        }
        
        // Mark coupon as used
        user.setCouponUsed(coupon, true);
        couponRepository.update(coupon);
        
        // Add amount to user balance
        BigDecimal amount = coupon.getAmount();
        user.addToBalance(amount);
        userRepository.update(user);
        
        System.out.println("Applied coupon "+couponCode+" for user "+user.getId()+", adding $"+ amount+" to balance");
        
        return amount;
    }
    
    /**
     * Create a new coupon
     * 
     * @param code The coupon code
     * @param amount The amount to add to balance
     * @return The created coupon
     */
    public Coupon createCoupon(String code, BigDecimal amount) {
        Coupon coupon = new Coupon(code, amount);
        couponRepository.save(coupon);
        
        System.out.println("Created new coupon: " + coupon);
        
        return coupon;
    }
}
