package com.greatCouturierGame.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

public class Player {

    private static final Logger logger = LogManager.getLogger(Player.class);

    public static void addDelay() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

}
