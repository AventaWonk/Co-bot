package com.greatCouturierGame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Main {
    public final static Logger logger = LogManager.getLogger("FileLogger");
    private static final String accountsFile = "accounts.txt";

    public static void main(String[] args) {
        Map<String, String> accountsList = new HashMap<>();
        try (BufferedReader bfr = new BufferedReader(new FileReader(Main.accountsFile))) {
            String accountString;
            while ((accountString = bfr.readLine()) != null) {
                String[] accountParts = accountString.split(":");
                accountsList.put(accountParts[0], accountParts[1]);
            }
        } catch (FileNotFoundException e) {
            Main.logger.fatal("File"+ Main.accountsFile +"not found");
        } catch (IOException e) {
            Main.logger.fatal("");
        } catch (NullPointerException e) {
            Main.logger.fatal("Bad accounts structure. Check!");
        }

        BiConsumer<String, String> engine = (uid, authToken) -> {
            Bot bot = new Bot(uid, authToken);
            Thread botThread = new Thread(bot);
            botThread.start();
        };
        accountsList.forEach(engine);
    }

}
