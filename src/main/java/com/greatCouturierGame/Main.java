package com.greatCouturierGame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public final static Logger logger = LogManager.getLogger("FileLogger");

    public static void main(String[] args) {
        Map<String, String> accountsList = new HashMap<>();
//        accountsList.put("", "");
//        accountsList.forEach();
//        try (FileReader fr = new FileReader("accounts.txt")) {
//            while (fr.) {
//
//            }
//        } catch (FileNotFoundException e) {
//            logger.error();
//        } catch (IOException e) {
//            logger.fatal();
//        }


        Thread botThread = new Thread(bot);
        botThread.start();
    }

}
