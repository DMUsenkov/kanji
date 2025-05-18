package ru.hse.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.hse.TelegramBotApplication;
import ru.hse.TelegramBotDataStubs;
import ru.hse.bot.EcommerceTelegramBot;
import ru.hse.exception.InsufficientBalanceException;
import ru.hse.exception.InsufficientStockException;
import ru.hse.model.CartItem;
import ru.hse.model.Product;
import ru.hse.model.User;
import ru.hse.service.OrderService;
import ru.hse.service.ProductService;
import ru.hse.service.UserService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StrangeCaseTests {
    private TelegramBotDataStubs stubs = new TelegramBotDataStubs();
    private TelegramBotApplication app;
    private EcommerceTelegramBot bot;
    StringBuffer botOut;
    private static final long userId = 1l;
    private ByteArrayOutputStream sysOutContent;
    private PrintStream outPS, originalOut;
    private User user = new User(1L,1L,"user_1",new HashSet<>());
    @BeforeEach
    public void beforeEach(){
        sysOutContent = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        try {
            originalOut = System.out;
            outPS = new PrintStream(sysOutContent, true, utf8);
            System.setOut(outPS);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    @AfterEach
    public void afterEach() throws IOException {
        botOut = null;
        bot = null;
        outPS.close();
        sysOutContent.close();
        System.setOut(originalOut);
    }
    private String reactOnMessage (String message){
        return reactOnMessage(message, userId);
    }
    private String reactOnMessage (String message, long userId){
        bot.onUpdateReceived(stubs.formUpdateRequest(message, userId));
        if(botOut!=null){
            try {
                return botOut.toString();
            } finally {
                botOut.setLength(0);
            }
        }
        return null;
    }
    @Test
    public void testNoGoods(){

        //app = new TelegramBotApplication(true);
        //app.startServices();
        bot = new EcommerceTelegramBot(
                new StringWriter(),
                "test",
                "test",
                new UserService(null){
                    @Override
                    public User getUserByChatId(Long chatId){
                        return user;
                    }
                },
                new ProductService(null){
                    @Override
                    public List<Product> getAllProducts(){
                        return Collections.emptyList();
                    }
                },
                null,
                null
        );
        botOut = bot.getTestWriter().getBuffer();
        reactOnMessage("/start");
        String res = reactOnMessage("/products");
        res = res.toLowerCase();
        assertTrue(res.contains("no products"));
    }
    @Test
    public void testExceptionSend(){

        //app = new TelegramBotApplication(true);
        //app.startServices();
        bot = new EcommerceTelegramBot(
                new StringWriter(),
                "test",
                "test",
                new UserService(null){
                    @Override
                    public User getUserByChatId(Long chatId){
                        return user;
                    }
                },
                new ProductService(null){
                    @Override
                    public List<Product> getAllProducts(){
                        return Collections.emptyList();
                    }
                },
                null,
                null
        );
        bot = Mockito.spy(bot);

        Mockito.doThrow(new RuntimeException("exception happened")).when(bot).sendMessage(1L,"Welcome back, USER_1! Your account is already set up.");

        botOut = bot.getTestWriter().getBuffer();
        String res = reactOnMessage("/start");
        res = res.toLowerCase();
        assertTrue(res.contains("an error occurred"));
    }
    @Test
    public void messageToNobody() throws IOException, TelegramApiException {

        //app = new TelegramBotApplication(true);
        //app.startServices();
        bot = new EcommerceTelegramBot(
                null,
                "test",
                "test",
                new UserService(null){
                    @Override
                    public User getUserByChatId(Long chatId){
                        return user;
                    }
                },
                new ProductService(null){
                    @Override
                    public List<Product> getAllProducts(){
                        return Collections.emptyList();
                    }
                },
                null,
                null
        );
        bot = Mockito.spy(bot);
        ByteArrayOutputStream errOutContent = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        PrintStream originalErr = System.err;
        try (PrintStream outErr = new PrintStream(errOutContent, true, utf8);){
            System.setErr(outErr);
            Mockito.doThrow(new TelegramApiException()).when(bot).execute(Mockito.any(SendMessage.class));
            reactOnMessage("/start");
            assertTrue(errOutContent.toString().contains("Error sending"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        finally{
            System.setErr(originalErr);
        }
        errOutContent.close();
    }
    @Test
    public void messageToNobody2() throws IOException, TelegramApiException {

        //app = new TelegramBotApplication(true);
        //app.startServices();
        bot = new EcommerceTelegramBot(
                null,
                "test",
                "test",
                new UserService(null){
                    @Override
                    public User getUserByChatId(Long chatId){
                        return user;
                    }
                },
                new ProductService(null){
                    @Override
                    public List<Product> getAllProducts(){
                        return Collections.emptyList();
                    }
                },
                null,
                null
        );
        bot = Mockito.spy(bot);
        Mockito.doReturn(null).when(bot).execute(Mockito.any(SendMessage.class));
        reactOnMessage("/start");
    }
    @Test
    public void orderExceptionInsufficientBalance() throws IOException, InsufficientBalanceException, InsufficientStockException {
        OrderService os = new OrderService(null, null, null );
        //app = new TelegramBotApplication(true);
        //app.startServices();
        os = Mockito.spy(os);

        bot = new EcommerceTelegramBot(
                new StringWriter(),
                "test",
                "test",
                new UserService(null){
                    @Override
                    public User getUserByChatId(Long chatId){
                        return user;
                    }
                },
                new ProductService(null){
                    @Override
                    public List<Product> getAllProducts(){
                        return Collections.emptyList();
                    }
                },
                os,
                null
        );
        botOut = bot.getTestWriter().getBuffer();
        user = Mockito.spy(user);
        List<CartItem> list = new LinkedList<>();
        list.add(null);
        Mockito.doReturn(list).when(user).getCart();
        Mockito.doThrow(new InsufficientBalanceException("")).when(os).createOrder(Mockito.any());
        String s = reactOnMessage("/cart checkout");
        assertTrue(s.toString().contains("Insufficient balance"));
    }
    @Test
    public void orderExceptionInsufficientStock() throws IOException, InsufficientStockException, InsufficientBalanceException {
        OrderService os = new OrderService(null, null, null );
        //app = new TelegramBotApplication(true);
        //app.startServices();
        os = Mockito.spy(os);

        bot = new EcommerceTelegramBot(
                new StringWriter(),
                "test",
                "test",
                new UserService(null){
                    @Override
                    public User getUserByChatId(Long chatId){
                        return user;
                    }
                },
                new ProductService(null){
                    @Override
                    public List<Product> getAllProducts(){
                        return Collections.emptyList();
                    }
                },
                os,
                null
        );
        botOut = bot.getTestWriter().getBuffer();
        user = Mockito.spy(user);
        List<CartItem> list = new LinkedList<>();
        list.add(null);
        Mockito.doReturn(list).when(user).getCart();
        Mockito.doThrow(new InsufficientStockException("")).when(os).createOrder(Mockito.any());
        String s = reactOnMessage("/cart checkout");
        assertTrue(s.toString().contains("Insufficient stock"));
    }
    @Test
    public void orderExceptionSome() throws IOException, InsufficientStockException, InsufficientBalanceException {
        OrderService os = new OrderService(null, null, null );
        //app = new TelegramBotApplication(true);
        //app.startServices();
        os = Mockito.spy(os);

        bot = new EcommerceTelegramBot(
                new StringWriter(),
                "test",
                "test",
                new UserService(null){
                    @Override
                    public User getUserByChatId(Long chatId){
                        return user;
                    }
                },
                new ProductService(null){
                    @Override
                    public List<Product> getAllProducts(){
                        return Collections.emptyList();
                    }
                },
                os,
                null
        );
        botOut = bot.getTestWriter().getBuffer();
        user = Mockito.spy(user);
        List<CartItem> list = new LinkedList<>();
        list.add(null);
        Mockito.doReturn(list).when(user).getCart();
        Mockito.doThrow(new RuntimeException("")).when(os).createOrder(Mockito.any());
        String s = reactOnMessage("/cart checkout");
        assertEquals("to 1: Error processing your order: \n", s.toString());
    }
}
