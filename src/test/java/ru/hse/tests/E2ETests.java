package ru.hse.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.hse.TelegramBotApplication;
import ru.hse.TelegramBotDataStubs;
import ru.hse.bot.EcommerceTelegramBot;
import ru.hse.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class E2ETests {
    private TelegramBotApplication app;
    private EcommerceTelegramBot bot;
    StringBuffer botOut;
    private static final long userId = 1l;
    private ByteArrayOutputStream sysOutContent;
    private PrintStream outPS, originalOut;
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
        app = new TelegramBotApplication(true);
        try {
            app.startServices();
            bot = app.getBot();
            botOut = app.getBotTestWriter().getBuffer();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    @AfterEach
    public void afterEach() throws IOException {
        outPS.close();
        sysOutContent.close();
        System.setOut(originalOut);
    }
    private String reactOnMessage (String message){
        return reactOnMessage(message, userId);
    }
    private String reactOnMessage (String message, long userId){
        bot.onUpdateReceived(app.getDataStubs().formUpdateRequest(message, userId));
        try {
            return botOut.toString();
        }finally{
            botOut.setLength(0);
        }
    }

    @Test
    public void testBotName(){
        assertEquals("${env.BOT_USERNAME}",bot.getBotUsername());
    }
    @Test
    public void testBeforeStart(){
        String res = reactOnMessage("/hello");
        assertTrue(res.length()>0);
        assertTrue(res.toLowerCase().contains("please use /start to create an account first"));
    }
    @Test
    public void testStart(){
        String res = reactOnMessage("/start");
        assertTrue(res.length()>0);
        res = res.toLowerCase();
        assertTrue(res.contains("user_1"));
    }
    @ParameterizedTest
    @ValueSource(strings = {"/help", "/account", "/orders", "/products", "/coupon", "/cart"})
    public void testMainMenuPoints(String point){
        reactOnMessage("/start");
        String res = reactOnMessage("/help");
        res = res.toLowerCase();
        assertTrue(res.contains(point));
    }

    @ParameterizedTest
    @ValueSource(strings = {"start", "help", "account", "orders", "products", "coupon", "cart"})
    public void testHelp(String point){
        reactOnMessage("/start");
        String res = reactOnMessage("/help "+point);
        res = res.toLowerCase();
        assertTrue(res.contains(point));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/help", "/cart", "/coupon"})
    public void testHelpFake(String point){
        reactOnMessage("/start");
        String res = reactOnMessage(point+ " deepseek");
        res = res.toLowerCase();
        assertTrue(res.contains("not recognized"));
    }
    @Test
    public void testWrongCommand(){
        reactOnMessage("/start");
        String res = reactOnMessage("/wrong");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("unknown command"));
    }
    @Test
    public void testNullMessage(){
        bot.onUpdateReceived(null);
        assertFalse(sysOutContent.toString().contains("Received message"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"good with high price\nprice: $4000", "good2\nprice: $500", "batflix"})
    public void testProducts(String point) {
        reactOnMessage("/start");
        String res = reactOnMessage("/products");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains(point));
    }

    @Test
    public void testBuyNonString(){
        reactOnMessage("/start");
        String res = reactOnMessage("/cart add string 1");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("invalid product id"));
    }

    @Test
    public void testBuyWrongGood(){
        reactOnMessage("/start");
        String res = reactOnMessage("/cart add 4 1");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("not found"));
    }
    @Test
    public void testBuyNoMoney(){
        reactOnMessage("/start");
        String res = reactOnMessage("/cart add 1 1");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("insufficient balance"));
    }
    @Test
    public void testBuyNotEnoughProduct(){
        reactOnMessage("/start");
        String res = reactOnMessage("/cart add 1 1000");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("not enough stock available"));
    }

    @Test
    public void testBuyWithoutANumber(){
        reactOnMessage("/start");
        String res = reactOnMessage("/cart add 3");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("invalid command"));
    }
    @Test
    public void testBuyNotExistProduct(){
        reactOnMessage("/start");
        String res = reactOnMessage("/cart add 4 1");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("not found"));
    }
    @Test
    public void testBuyAndCheckout(){
        reactOnMessage("/start");
        reactOnMessage("/coupon apply 3DRwBBrcFThKXq9zNIdPihfg3eaQ7g");
        reactOnMessage("/cart add 2 1");
        String res = reactOnMessage("/cart checkout");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("balance: $500"));
    }
    @Test
    public void testBucketIncorrect(){
        reactOnMessage("/start");
        reactOnMessage("/coupon apply 3DRwBBrcFThKXq9zNIdPihfg3eaQ7g");
        reactOnMessage("/start",2);//user 2
        reactOnMessage("/coupon apply 3DRwBBrcFThKXq9zNIdPihfg3eaQ7g", 2);
        reactOnMessage("/cart add 5 1");
        reactOnMessage("/cart add 5 1",2);
        reactOnMessage("/cart checkout");
        String res = reactOnMessage("/cart add 5 1",2);

        res = reactOnMessage("/cart add 1 ff");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("invalid"));

        res = reactOnMessage("/cart add 1 -10");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("greater than zero"));
    }
    @Test
    public void testBucketAddRemove(){
        reactOnMessage("/start");
        reactOnMessage("/coupon apply 3DRwBBrcFThKXq9zNIdPihfg3eaQ7g");
        reactOnMessage("/cart add 1 10");
        String res = reactOnMessage("/cart clear");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("been cleared"));
    }

    @Test
    public void testBucketClear(){
        reactOnMessage("/start");
        reactOnMessage("/coupon apply 3DRwBBrcFThKXq9zNIdPihfg3eaQ7g");
        reactOnMessage("/cart add 3 10");
        reactOnMessage("/cart clear");
        String res = reactOnMessage("/cart");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("is empty"));
    }

    @Test
    public void testCheckoutEmpty(){
        reactOnMessage("/start");
        String res = reactOnMessage("/cart checkout");
        assertTrue(res.contains("is empty"));
    }

    @Test
    public void testCheckoutSuccessOrder(){
        reactOnMessage("/start");
        reactOnMessage("/coupon apply 3DRwBBrcFThKXq9zNIdPihfg3eaQ7g");
        reactOnMessage("/cart add 2 1");
        reactOnMessage("/cart checkout");
        String res = reactOnMessage("/orders");
        assertTrue(res.contains("Items: 1"));
    }

    @Test
    public void testCartInfo(){
        reactOnMessage("/start");
        reactOnMessage("/coupon apply 3DRwBBrcFThKXq9zNIdPihfg3eaQ7g");
        reactOnMessage("/cart add 2 1");
        String res = reactOnMessage("/cart");
        assertTrue(res.contains("Total: $500"));
    }
    @Test
    public void testBucketRemoveCheckout(){
        reactOnMessage("/start");
        reactOnMessage("/coupon apply 3DRwBBrcFThKXq9zNIdPihfg3eaQ7g");
        reactOnMessage("/cart add 2 1");
        reactOnMessage("/cart clear");
        reactOnMessage("/cart add 2 1");
        String res = reactOnMessage("/cart checkout");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("balance: $500"));
    }

    @Test
    public void testOrderFullInfo(){
        reactOnMessage("/start");
        String res = reactOnMessage("/orders");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("yet"));
    }
    @Test
    public void accountInitialTest(){
        reactOnMessage("/start");
        String res;
        res = reactOnMessage("/account");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("balance: $0"));
    }
    @Test
    public void accountTestMoney(){
        String res;
        reactOnMessage("/start");
        reactOnMessage("/coupon apply 3DRwBBrcFThKXq9zNIdPihfg3eaQ7g");
        res = reactOnMessage("/account");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("balance: $1000"));
    }
    @Test
    public void couponsTestInitial(){
        String res;
        reactOnMessage("/start");
        res = reactOnMessage("/coupon");
        assertNotNull(res);
        res = res.toLowerCase();
        assertEquals(res,"to 1: already applied coupons: \n\n");
    }
    @Test
    public void couponsTestUsed(){
        String res;
        reactOnMessage("/start");
        reactOnMessage("/coupon apply 3DRwBBrcFThKXq9zNIdPihfg3eaQ7g");
        res = reactOnMessage("/coupon");
        assertNotNull(res);
        assertTrue(res.contains("Already"));
    }

    @Test
    public void testCouponsIncorrect(){
        reactOnMessage("/start");
        String res = reactOnMessage("/coupon apply 1");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("is not found"));
        reactOnMessage("/coupon apply 3DRwBBrcFThKXq9zNIdPihfg3eaQ7g");
        res = reactOnMessage("/coupon apply 3DRwBBrcFThKXq9zNIdPihfg3eaQ7g");

        res = reactOnMessage("/account");
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("balance: $1000"));
    }
    @Test
    public void testClosing(){
        reactOnMessage("/start");
        app.stop();
        assertTrue(sysOutContent.toString().contains("Bot is s"));
    }
}
