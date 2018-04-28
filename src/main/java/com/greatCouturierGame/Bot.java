package com.greatCouturierGame;

import org.apache.logging.log4j.Logger;

import java.util.Date;

public class Bot implements Runnable {

    private String uid;
    private String authToken;
    private static Logger logger = Main.getLogger();

    public Bot(String uid, String authToken) {
        this.uid = uid;
        this.authToken = authToken;
    }

    public void run() {

        while (true) {
            final Gamer gamer = new Gamer(uid, authToken);

            final String nick = gamer.getModelNick();
            final int money = gamer.getModelMoney();
            final int rating = gamer.getModelRating();
            final long[] sat = gamer.getSkillsAvTime();
            final long currentTime = new Date().getTime();

            logger.info("New cycle started!");
            for (int i = 0; i < 5; i++) {
                // Check is new tech available now?
                if (gamer.isNewTechAvailable()) {
                    String[] availableTechIds = gamer.getAvailableTechIds();
                    gamer.researchTech(availableTechIds[0]);
                    logger.info("New tech was researched");
                }

                final long skillAvTime = sat[i];
                if (skillAvTime >= currentTime) {
                    final int skillCooldown = (int) Math.ceil((skillAvTime - currentTime) / 1000);
                    logger.info("Skill №" +(i+1)+ " is not available now! Next in "+ Bot.getFormattedTime(skillCooldown));
                    continue;
                }

                try {
                    final int taskTimeInc = gamer.doTask(i);
                    sat[i] = new Date().getTime() + taskTimeInc + 3000;
                    logger.info("Skill №" + (i+1) + " successfully applied!");

                    // Simulate gamers reaction time
                    Thread.sleep(2400);
                } catch (Exception e) {
                    logger.error(e);
                }

            }

            int soonSkillNumber = 0;
            for (int i = 1; i < 5; i++) {
                final long l = sat[i];
                if (l < sat[soonSkillNumber] && l > currentTime) {
                    soonSkillNumber = i;
                }
            }

            long soonSkillAvTime = sat[soonSkillNumber] - new Date().getTime();
            logger.info("Skill №"+ (soonSkillNumber+1) +". Waiting "+ Bot.getFormattedTime(soonSkillAvTime/1000) + "\n");
            gamer.closeGame();

            try {
                Thread.sleep(soonSkillAvTime + 10000);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
    }

    private static String getFormattedTime(long seconds) {
        long h = (long) Math.floor(seconds / 3600) ;
        long m = (long) Math.floor(seconds % 3600 / 60) ;
        long s = (long) seconds % 3600 % 60;

        return h +":"+ m +":"+ s;
    }
}
