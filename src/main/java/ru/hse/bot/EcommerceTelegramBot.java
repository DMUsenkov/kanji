package ru.hse.bot;

import org.glassfish.grizzly.streams.StreamWriter;
import org.telegram.telegrambots.meta.api.objects.Chat;
import ru.hse.exception.*;
import ru.hse.model.CartItem;
import ru.hse.model.Order;
import ru.hse.model.Product;
import ru.hse.model.User;
import ru.hse.service.CouponService;
import ru.hse.service.OrderService;
import ru.hse.service.ProductService;
import ru.hse.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The main bot class that handles all Telegram interactions
 */
public class EcommerceTelegramBot extends TelegramLongPollingBot {
    private static final String HELP_TEXT_START = "/start - Start the bot and create an account";
    private static final String HELP_TEXT_ACCOUNT = "/account - Check your account balance";
    private static final String HELP_TEXT_ORDERS = "/orders Show you active orders";
    private static final String HELP_TEXT_GOODS = "/products - Show all available products";
    private static final String HELP_TEXT_COUPON = "/coupon or /coupon list - Show all applied coupons\n" +
                                                    "/coupon apply <coupon> - Apply selected coupon";
    private static final String HELP_TEXT_CART = "/cart or /cart list - Show product in current cart\n" +
            "/cart add <product> <quantity> - Add selected product to cart\n" +
            "/cart clear - Remove all products from you cart\n" +
            "/cart checkout - Create new order with selected products from cart";
    private static final String HELP_TEXT = "Available commands:\n" +
            HELP_TEXT_START + "\n" +
            "/help - Show this help message\n" +
            "/help <command> - Show the help message for corresponding command\n" +
            HELP_TEXT_ACCOUNT + "\n" +
            HELP_TEXT_ORDERS + "\n" +
            HELP_TEXT_GOODS+"\n" +
            "/coupon - Apply coupon or list all applied coupons\n" +
            "/cart - Show you current cart, buy goods from store, checkout";
    private static final String HELP_TEXT_UNKNOWN = "Command not recognized. Send /help to list all available commands.";
    private final StringWriter testWriter;
    private final String botUsername;
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;

    public EcommerceTelegramBot( StringWriter testWriter,
                                String botUsername, String botToken,
                                UserService userService,
                                ProductService productService,
                                OrderService orderService,
                                CouponService couponService) {
        super(botToken);
        this.testWriter = testWriter;
        this.botUsername = botUsername;
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.couponService = couponService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
    public StringWriter getTestWriter(){
        return testWriter;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null || !update.getMessage().hasText()) {
            return;
        }

        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.getText();
        String firstName = message.getFrom().getFirstName();
        
        System.out.println("Received message: '"+text+"' from user "+firstName+" ("+chatId+")");

        try {
            // Check if user exists, if not create a new account on any command
            User user = userService.getUserByChatId(chatId);
            if (user == null && !text.startsWith("/start")) {
                sendMessage(chatId, "Please use /start to create an account first.");
                return;
            }
            if (text.startsWith("/start")) {
                handleStartCommand(chatId, firstName);
            } else if (text.startsWith("/help")) {
                handleHelpCommand(chatId, text);
            }  else if (text.startsWith("/account")) {
                handleAccountCommand(chatId);
            } else if (text.startsWith("/orders")) {
                handleOrdersCommand(chatId);
            } else if (text.startsWith("/products")) {
                handleGoodsCommand(chatId);
            } else if (text.startsWith("/coupon")) {
                handleCouponCommand(chatId, text);
            } else if (text.startsWith("/cart")) {
                handleCartCommand(chatId, text);
            } else {
                sendMessage(chatId, "Unknown command. Type /help to see available commands.");
            }
        } catch (Exception e) {
            System.err.println("Error processing message");
            e.printStackTrace();
            sendMessage(chatId, "An error occurred: " + e.getMessage());
        }
    }

    private void handleStartCommand(Long chatId, String firstName) {
        User existingUser = userService.getUserByChatId(chatId);
        
        if (existingUser != null) {
            sendMessage(chatId, "Welcome back, " + firstName + "! Your account is already set up.");
        } else {
            User newUser = userService.createUser(chatId, firstName);
            sendMessage(chatId, "Welcome, " + firstName + "! Your account has been created with an initial balance of $" + 
                    newUser.getBalance() + ". Type /help to see available commands.");
        }
    }

    private void handleHelpCommand(Long chatId, String text) {
        text = text.substring(5).trim();
        if(text.isEmpty() || text.equals("help"))
            sendMessage(chatId, HELP_TEXT);
        else
            switch (text){
                case "start":
                    sendMessage(chatId, HELP_TEXT_START);
                    break;
                case "account":
                    sendMessage(chatId, HELP_TEXT_ACCOUNT);
                    break;
                case "orders":
                    sendMessage(chatId, HELP_TEXT_ORDERS);
                    break;
                case "products":
                    sendMessage(chatId, HELP_TEXT_GOODS);
                    break;
                case "coupon":
                    sendMessage(chatId, HELP_TEXT_COUPON);
                    break;
                case "cart":
                    sendMessage(chatId, HELP_TEXT_CART);
                    break;
            }
        sendMessage(chatId, HELP_TEXT_UNKNOWN);
    }

    private void handleAccountCommand(Long chatId) {
        User user = userService.getUserByChatId(chatId);
        sendMessage(chatId, "Your current balance: $" + user.getBalance() +
                "\n\nUse /coupon apply <code> to add funds to your balance.");
    }

    private void handleOrdersCommand(Long chatId) {
        User user = userService.getUserByChatId(chatId);
        List<Order> orders = orderService.getUserOrders(user);

        if (orders.isEmpty()) {
            sendMessage(chatId, "You haven't placed any orders yet.");
            return;
        }

        StringBuilder ordersMsg = new StringBuilder("Your Order History:\n\n");

        for (Order order : orders) {
            ordersMsg.append("Order ID: ").append(order.getId())
                    .append("\nDate: ").append(order.getCreatedAt())
                    .append("\nTotal: $").append(order.getTotal())
                    .append("\nItems: ").append(order.getItems().size())
                    .append("\n\n");
        }

        sendMessage(chatId, ordersMsg.toString());
    }
    private void handleGoodsCommand(Long chatId) {
        List<Product> products = productService.getAllProducts();

        if (products.isEmpty()) {
            sendMessage(chatId, "No products available at the moment.");
            return;
        }

        StringBuilder response = new StringBuilder("Available Products:\n\n");

        for (Product product : products) {
            response.append("ID: ").append(product.getId())
                    .append("\nName: ").append(product.getName())
                    .append("\nPrice: $").append(product.getPrice())
                    .append("\nIn Stock: ").append(product.getQuantity())
                    .append("\n\nTo add to cart: /cart add ")
                    .append(product.getId()).append(" <quantity>\n\n");
        }

        sendMessage(chatId, response.toString());
    }

    private void handleCouponCommand(Long chatId, String text) {
        text = text.substring(7).trim();
        if(text.isEmpty() || text.equals("list")) {
            handleCouponListCommand(chatId);
            return;
        }
        if(text.startsWith("apply")){
            handleCouponApplyCommand(chatId, text);
            return;
        }
        sendMessage(chatId, HELP_TEXT_UNKNOWN);
    }
    private void handleCouponListCommand(Long chatId){
        StringBuilder couponMsg = new StringBuilder("Already applied coupons: \n");
        User user = userService.getUserByChatId(chatId);
        for(String s: user.getCouponsUsed()){
            couponMsg.append("\n");
            couponMsg.append(s);
        }
        sendMessage(chatId, couponMsg.toString());
    }

    private void handleCouponApplyCommand(Long chatId, String text){
        User user = userService.getUserByChatId(chatId);
        text = text.substring(5).trim();
        StringBuilder couponMsg = new StringBuilder("Coupon ");
        couponMsg.append(text);
        try {
            couponService.applyCoupon(user, text);
            couponMsg.append(" was successfully applied\n");
            couponMsg.append("\n\nYour balance: $").append(user.getBalance());
            sendMessage(chatId, couponMsg.toString());
        }catch(CouponNotFoundException cnfe){
            couponMsg.append(" is not found.");
            sendMessage(chatId, couponMsg.toString());
        } catch (CouponWasUsedException e) {
            couponMsg.append(" was already applied.");
            sendMessage(chatId, couponMsg.toString());
        }
    }
    private void handleCartCommand(Long chatId, String text) {
        text = text.substring(5).trim();
        if(text.isEmpty() || text.equals("list")){
            handleCartInfoCommand(chatId);
            return;
        }
        if(text.equals("clear")){
            handleClearCartCommand(chatId);
            return;
        }
        if(text.startsWith("add")){
            handleAddToCartCommand(chatId, text);
            return;
        }
        if(text.equals("checkout")){
            handleCheckoutCommand(chatId);
            return;
        }
        sendMessage(chatId, HELP_TEXT_UNKNOWN);
    }
    private void handleCartInfoCommand(Long chatId) {
        User user = userService.getUserByChatId(chatId);
        List<CartItem> cart = user.getCart();

        if (cart.isEmpty()) {
            sendMessage(chatId, "Your shopping cart is empty. Use /products to browse available products.");
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        StringBuilder cartMsg = new StringBuilder("Your Shopping Cart:\n\n");

        for (CartItem item : cart) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));

            cartMsg.append(quantity).append("x ").append(product.getName())
                    .append(" - $").append(product.getPrice()).append(" each")
                    .append(" = $").append(itemTotal).append("\n");

            total = total.add(itemTotal);
        }

        cartMsg.append("\nTotal: $").append(total);
        cartMsg.append("\n\nYour balance: $").append(user.getBalance());
        cartMsg.append("\n\nUse /cart checkout to complete your purchase or /cart clear to empty your cart.");

        sendMessage(chatId, cartMsg.toString());
    }

    private void handleClearCartCommand(Long chatId) {
        User user = userService.getUserByChatId(chatId);
        userService.clearCart(user);
        sendMessage(chatId, "Your shopping cart has been cleared.");
    }
    private void handleAddToCartCommand(Long chatId, String text) {
        String[] parts = text.split("\\s+");

        if (parts.length < 3) {
            sendMessage(chatId, "Invalid command format. Use: /cart add <product_id> <quantity>");
            return;
        }

        try {
            Long productId = Long.parseLong(parts[1]);
            int quantity = Integer.parseInt(parts[2]);

            if (quantity <= 0) {
                sendMessage(chatId, "Quantity must be greater than zero.");
                return;
            }

            User user = userService.getUserByChatId(chatId);
            Product product = productService.getProductById(productId);

            if (product.getQuantity() < quantity) {
                sendMessage(chatId, "Not enough stock available. Current stock: " + product.getQuantity());
                return;
            }
            double balance = user.getBalance().doubleValue();
            balance -= product.getPrice().doubleValue() * quantity;
            balance -= user.getCartTotal().doubleValue();
            if(balance < 0) {
                sendMessage(chatId, "Insufficient balance: "+
                        "\nUse /coupon apply <code> to add funds to your balance." +
                        "\n Current balance " + user.getBalance()+"\n"+" Current cart cost: "+user.getCartTotal());
                return;
            }

            userService.addToCart(user, product, quantity);

            sendMessage(chatId, quantity + "x " + product.getName() + " added to your cart.\n" +
                    "Use /cart to view your cart or /checkout to complete your purchase.");

        } catch (NumberFormatException e) {
            sendMessage(chatId, "Invalid product ID or quantity. Please use numbers only.");
        } catch (ProductNotFoundException e) {
            sendMessage(chatId, e.getMessage());
        }
    }

    private void handleCheckoutCommand(Long chatId) {
        User user = userService.getUserByChatId(chatId);
        
        if (user.getCart().isEmpty()) {
            sendMessage(chatId, "Your shopping cart is empty. Use /products to browse available products.");
            return;
        }
        
        try {
            Order order = orderService.createOrder(user);
            
            StringBuilder orderMsg = new StringBuilder("Order placed successfully!\n\n");
            orderMsg.append("Order ID: ").append(order.getId()).append("\n");
            orderMsg.append("Date: ").append(order.getCreatedAt()).append("\n\n");
            orderMsg.append("Items:\n");
            
            for (CartItem item : order.getItems()) {
                orderMsg.append("- ").append(item.getQuantity()).append("x ")
                       .append(item.getProduct().getName())
                       .append(" ($").append(item.getProduct().getPrice()).append(" each)\n");
            }
            
            orderMsg.append("\nTotal: $").append(order.getTotal());
            orderMsg.append("\nRemaining balance: $").append(user.getBalance());
            orderMsg.append("\n\nThank you for your purchase!");
            
            sendMessage(chatId, orderMsg.toString());
            
        } catch (InsufficientBalanceException e) {
            sendMessage(chatId, "Insufficient balance: " + e.getMessage() + 
                    "\nUse /coupon apply <code> to add funds to your balance.");
        } catch (InsufficientStockException e) {
            sendMessage(chatId, "Insufficient stock: " + e.getMessage() + 
                    "\nPlease update your cart with available quantities.");
        } catch (Exception e) {
            sendMessage(chatId, "Error processing your order: " + e.getMessage());
        }
    }

    public void sendMessage(Long chatId, String text) {
        if(testWriter!=null)
        {
            testWriter.append("to ");
            testWriter.append(Long.toString(chatId));
            testWriter.append(": ");
            testWriter.append(text);
            testWriter.append("\n");
        }else {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                System.err.println("Error sending message");
            }
        }
    }
    
    public void onClosing() {
        System.out.println("Bot is shutting down...");
    }
}
