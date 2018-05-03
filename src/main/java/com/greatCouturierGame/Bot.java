package com.greatCouturierGame;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Date;

public class Bot implements Runnable {

    private String uid;
    private String authToken;
    private GameGateway gameApi;
    private static Logger logger = Main.getLogger();

    Bot(String uid, String authToken) {
        this.uid = uid;
        this.authToken = authToken;
    }

    public void run() {

        while (true) {
            final GameGateway gameApi = new GameGateway(uid, authToken);
            this.gameApi = gameApi;
            final long[] sat = gameApi.getSkillsAvailabilityTime();
            final long currentTime = new Date().getTime();

            if (gameApi.isPodiumShouldResolve()) {
                try {
                    gameApi.resolvePodiumStatus();
                } catch (IOException e) {
                    System.exit(0);
                }
            }

            if (gameApi.isPodiumAvailable()) {
                try {
                    gameApi.enterPodium();
                } catch (IOException e) {
                    System.exit(0);
                }
            }

            for (int i = 0; i < 5; i++) {
                final long skillAvTime = sat[i];
                if (skillAvTime > currentTime) {
//                    final int skillCooldown = (int) Math.ceil((skillAvTime - currentTime) / 1000);
                    continue;
                }

                // New type (podium) availability check
                if (gameApi.isNewTypesAvailable()) {
                    String[] availableTypesIds = gameApi.getAvailableTypesIds();
                    gameApi.researchNewType(availableTypesIds[0]);
                    logger.info("New type successfully researched");
                }

                // New tech availability check
                if (gameApi.isNewTechsAvailable()) {
                    String[] availableTechIds = gameApi.getAvailableTechsIds();
                    gameApi.researchNewTech(availableTechIds[0]);
                    logger.info("New tech was successfully researched");
                }

                try {
                    final int taskTimeInc = gameApi.doTask(i);
                    sat[i] = new Date().getTime() + taskTimeInc + 3000;
                    logger.info("Skill №" + (i+1) + " was successfully applied!");

                    // Simulate gamers reaction time
                    Thread.sleep(2400);
                } catch (Exception e) {
                    logger.error(e);
                    System.exit(0);
                }

            }

            int soonSkillNumber = 0;
            for (int i = 1; i < 5; i++) {
                final long skillAvTime = sat[i];
                if (skillAvTime < sat[soonSkillNumber] && skillAvTime > currentTime) {
                    soonSkillNumber = i;
                }
            }

            long soonSkillAvTime = sat[soonSkillNumber] - new Date().getTime();
            logger.info("Skill №"+ (soonSkillNumber+1) +". Waiting "+ Bot.getFormattedTime(soonSkillAvTime/1000) + "\n");
            gameApi.closeGame();

            try {
                gameApi.closeGame();
                Thread.sleep(soonSkillAvTime + 10000);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
    }

    private static String getFormattedTime(long seconds) {
        long h = (long) Math.floor(seconds / 3600) ;
        long m = (long) Math.floor(seconds % 3600 / 60) ;
        long s = seconds % 3600 % 60;

        return h +":"+ m +":"+ s;
    }
}
