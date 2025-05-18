package ru.hse;

import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.forum.*;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.games.Game;
import org.telegram.telegrambots.meta.api.objects.passport.PassportData;
import org.telegram.telegrambots.meta.api.objects.payments.Invoice;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.api.objects.videochat.VideoChatEnded;
import org.telegram.telegrambots.meta.api.objects.videochat.VideoChatParticipantsInvited;
import org.telegram.telegrambots.meta.api.objects.videochat.VideoChatScheduled;
import org.telegram.telegrambots.meta.api.objects.videochat.VideoChatStarted;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppData;
import ru.hse.bot.EcommerceTelegramBot;
import ru.hse.model.Product;
import ru.hse.repository.CouponRepository;
import ru.hse.repository.OrderRepository;
import ru.hse.repository.ProductRepository;
import ru.hse.repository.UserRepository;
import ru.hse.service.CouponService;
import ru.hse.service.OrderService;
import ru.hse.service.ProductService;
import ru.hse.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

/**
 * Main application class that initializes all components and starts the Telegram bot
 */
public class TelegramBotApplication {
    private Properties properties;
    private EcommerceTelegramBot bot;
    private TelegramBotDataStubs stubs = null;

    private boolean consoleMode = false;
    
    public TelegramBotApplication(boolean consoleMode) {
        this.consoleMode = consoleMode;
        loadProperties();
    }
    
    public void startServices() throws TelegramApiException {
        // Initialize repositories
        UserRepository userRepository = new UserRepository();
        ProductRepository productRepository = new ProductRepository();
        OrderRepository orderRepository = new OrderRepository();
        CouponRepository couponRepository = new CouponRepository();
        
        // Initialize services
        UserService userService = new UserService(userRepository);
        ProductService productService = new ProductService(productRepository);
        OrderService orderService = new OrderService(orderRepository, productRepository, userRepository);
        CouponService couponService = new CouponService(couponRepository, userRepository);
        
        // Load initial data
        loadInitialProducts(productRepository);
        loadCoupons(couponRepository);
        
        // Initialize and register the bot - get credentials directly from environment
        String botUsername = properties.getProperty("telegram.bot.username");
        String botToken = properties.getProperty("telegram.bot.token");
        
        // Log for debugging (not showing the full token for security)
        if (botToken != null && botToken.length() > 4) {
            System.out.println("Bot token found, first 4 chars: " + botToken.substring(0, 4));
        } else {
            System.err.println("Bot token is missing or invalid");
            throw new IllegalArgumentException("Bot token is missing or invalid");
        }
        
        if (botUsername == null || botUsername.isEmpty()) {
            System.err.println("Bot username is missing");
            throw new IllegalArgumentException("Bot username is missing");
        }

        System.out.println("Using bot username: " + botUsername);

        bot = new EcommerceTelegramBot(
            consoleMode?new StringWriter():null,
            botUsername, 
            botToken, 
            userService, 
            productService, 
            orderService, 
            couponService
        );
        if(consoleMode)
            stubs = new TelegramBotDataStubs();
        System.out.println("Bot registered successfully with username: " + botUsername);
    }
    public void startBot() throws TelegramApiException {
        if(consoleMode){
            StringWriter sw = bot.getTestWriter();
            Scanner sc = new Scanner(System.in); //System.in is a standard input stream
            bot.onUpdateReceived(stubs.formUpdateRequest("/start", 1));
            StringBuffer sb = sw.getBuffer();
            System.out.println(sb);sb.setLength(0);
            String read = sc.nextLine();
            while(!read.equals("exit")){
                bot.onUpdateReceived(stubs.formUpdateRequest(read, 1));
                System.out.println(sb);sb.setLength(0);
                read = sc.nextLine();
            }
        }else {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
        }
    }
    public void stop() {
        if (bot != null) {
            bot.onClosing();
        }
    }
    
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("Unable to find application.properties");
                throw new RuntimeException("Unable to find application.properties");
            }
            properties.load(input);
            System.out.println("Properties loaded successfully");
        } catch (IOException e) {
            System.err.println("Failed to load properties");
            e.printStackTrace();
            throw new RuntimeException("Failed to load properties", e);
        }
    }
    
    private void loadInitialProducts(ProductRepository productRepository) {
        String productsData = properties.getProperty("app.initial.products");
        if (productsData != null && !productsData.isEmpty()) {
            String[] products = productsData.split(";");
            for (String productData : products) {
                String[] parts = productData.split(",");
                if (parts.length == 4) {
                    try {
                        Long id = Long.parseLong(parts[0]);
                        String name = parts[1];
                        BigDecimal price = new BigDecimal(parts[2]);
                        int quantity = Integer.parseInt(parts[3]);
                        
                        Product product = new Product(id, name, price, quantity);
                        productRepository.save(product);
                        System.out.println("Added product: " + product);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing product data: "+productData);
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    private void loadCoupons(CouponRepository couponRepository) {
        String couponsData = properties.getProperty("app.coupons");
        if (couponsData != null && !couponsData.isEmpty()) {
            String[] coupons = couponsData.split(";");
            for (String couponData : coupons) {
                String[] parts = couponData.split(",");
                if (parts.length == 2) {
                    try {
                        String code = parts[0];
                        BigDecimal amount = new BigDecimal(parts[1]);
                        
                        couponRepository.addCoupon(code, amount);
                        System.out.println("Added coupon: "+code+" with amount "+amount);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing coupon data: "+couponData);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public EcommerceTelegramBot getBot() {
        return bot;
    }

    public StringWriter getBotTestWriter() {
        if(bot == null)
            return null;
        return bot.getTestWriter();
    }

    public TelegramBotDataStubs getDataStubs() {
        return stubs;
    }
}
