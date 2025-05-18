package ru.hse;

/**
 * Main entry point for the Telegram Bot E-commerce application
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Starting E-commerce Telegram Bot application...");
        boolean consoleMode = args[0].equals("test");

        try {
            TelegramBotApplication application = new TelegramBotApplication(consoleMode);
            // Add shutdown hook to gracefully stop the application
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down application...");
                application.stop();
                System.out.println("Application stopped successfully.");
            }));
            application.startServices();
            application.startBot();
            System.out.println("E-commerce Telegram Bot is now running. Press Ctrl+C to stop.");
        } catch (Exception e) {
            System.err.println("Failed to start application");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
