package com.greatCouturierGame.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Bot {

    private static final Logger logger = LogManager.getLogger(Bot.class);

    private AbstractBotStrategy botStrategy;

    public Bot(AbstractBotStrategy botStrategy) {
        this.botStrategy = botStrategy;
    }

    public void run(String uid, String authToken) {
        long next = this.botStrategy.doPlayerActions(uid, authToken);
    }

    public void setBotStrategy(AbstractBotStrategy botStrategy) {
        this.botStrategy = botStrategy;
    }
}
