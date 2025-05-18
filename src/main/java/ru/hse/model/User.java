package ru.hse.model;

import java.math.BigDecimal;
import java.util.*;

/**
 * Represents a user in the e-commerce system
 */
public class User {
    private Long id;
    private Long chatId;
    private String name;
    private BigDecimal balance;
    private List<CartItem> cart;
    private Set<String> couponUsed;

    public User(Long id, Long chatId, String name, Set<String> couponUsed) {
        this.id = id;
        this.chatId = chatId;
        this.name = name;
        this.balance = BigDecimal.valueOf(0);  // Start with zero balance
        this.cart = new ArrayList<>();
        this.couponUsed = couponUsed;
    }

    public Long getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void addToBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void subtractFromBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public List<CartItem> getCart() {
        return cart;
    }

    public void addToCart(Product product, int quantity) {
        // Check if the product is already in the cart
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(product.getId())) {
                // Update quantity of existing item
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        // Add new item to cart
        cart.add(new CartItem(product, quantity));
    }

    public void updateCartItemQuantity(Long productId, int quantity) {
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(productId)) {
                if (quantity <= 0) {
                    cart.remove(item);
                } else {
                    item.setQuantity(quantity);
                }
                return;
            }
        }
    }

    public void clearCart() {
        cart.clear();
    }

    public BigDecimal getCartTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            BigDecimal itemPrice = item.getProduct().getPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }
        return total;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", cartItems=" + cart.size() +
                '}';
    }
    public Collection<String> getCouponsUsed() {
        return Collections.unmodifiableCollection(couponUsed);
    }
    public boolean checkCouponUsed(Coupon coupon) {
        return couponUsed.contains(coupon.getCode());
    }

    public void setCouponUsed(Coupon coupon, boolean b) {
        couponUsed.add(coupon.getCode());
    }
}
