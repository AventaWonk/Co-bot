package com.greatCouturierGame;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class Bot implements Runnable {

    private String uid;
    private String authToken;
    private GameGateway gameApi;
    private long[] skillNextTime = {-1, -1, -1, -1};
    private static Logger logger = Main.getLogger();

    Bot(String uid, String authToken) {
        this.uid = uid;
        this.authToken = authToken;
    }

    public void run() {

        while (true) {
            final GameGateway gameApi = new GameGateway(uid, authToken);
            final long currentTime = new Date().getTime();
            this.gameApi = gameApi;
            if (this.skillNextTime[0] == -1) {
                this.skillNextTime = gameApi.getSkillsAvailabilityTime();
            }

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
                final long skillAvTime = this.skillNextTime[i];
                if (skillAvTime > currentTime) {
                    continue;
                }

                // New type (podium) availability check
                if (gameApi.isNewTypesAvailable()) {
                    String[] availableTypesIds = gameApi.getAvailableTypesIds();
                    final int typeId = ThreadLocalRandom.current().nextInt(availableTypesIds.length);
                    gameApi.researchNewType(availableTypesIds[typeId]);
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
                    final int generatedTimeInc = ThreadLocalRandom.current().nextInt(5000, 25000);
                    this.skillNextTime[i] = new Date().getTime() + taskTimeInc + generatedTimeInc;
                    logger.info("Skill №" + (i+1) + " was successfully applied!");

                    // Simulate gamers reaction time before next skill
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1500, 5000));
                } catch (Exception e) {
                    logger.error(e);
                    System.exit(0);
                }

            }

            int soonSkillNumber = 0;
            for (int i = 1; i < 5; i++) {
                final long skillAvTime = this.skillNextTime[i];
                if (skillAvTime < this.skillNextTime[soonSkillNumber] && skillAvTime > currentTime) {
                    soonSkillNumber = i;
                }
            }

            long soonSkillAvTime = this.skillNextTime[soonSkillNumber] - new Date().getTime();
            logger.info("Skill №"+ (soonSkillNumber+1) +". Waiting "+ Bot.getFormattedTime(soonSkillAvTime/1000));
            gameApi.closeGame();

            try {
                gameApi.closeGame();
                Thread.sleep(soonSkillAvTime);
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
