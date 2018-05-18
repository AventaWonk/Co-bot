package com.greatCouturierGame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Bot implements Runnable {

    private String uid;
    private String authToken;
    private GameGateway gameApi;

    Bot(String uid, String authToken) {
        this.uid = uid;
        this.authToken = authToken;
    }

    public void run() {
        while (true) {
            this.gameApi = new GameGateway(uid, authToken);
            List<Long> actionsNextTime = new ArrayList<>();
            try {
                actionsNextTime.add(this.podiumAction());
                actionsNextTime.add(this.shopAction());
                actionsNextTime.add(this.skillAction());
            } catch (IOException e) {
                System.exit(0);
            } finally {
                this.gameApi.closeGame();
            }
            
            try {
                Thread.sleep(actionsNextTime.stream().min(Long::compareTo).get());
            } catch (InterruptedException e) {
                System.exit(0);
            }
        }
    }

    private long shopAction() throws IOException {
        final long currentTime = System.currentTimeMillis();
        final List<Long> sellingWearFinishTime = gameApi.getSellingWearEndTime();
        boolean shopStatusFlag = false;
        for (Long wearEndTime : sellingWearFinishTime) {
            if (wearEndTime > currentTime) {
                shopStatusFlag = true;
                break;
            }
        }

        if (shopStatusFlag) {
            final String[] soldWeaIds = gameApi.resolveShopStatus();
            for (String wearId : soldWeaIds) {
                gameApi.completeSale(wearId);
                try {
                    Thread.sleep(988);
                } catch (InterruptedException e) {
                    System.exit(0);
                }
            }
        }

        while (sellingWearFinishTime.size() < 4) {
            Wear wear = Wear.generateRandomWear(
                    this.gameApi.getMaxWearTypesIds(),
                    this.gameApi.getMaxColorId(),
                    this.gameApi.getMaxTextureId(),
                    this.gameApi.getMaxTextureColorId()
            );
            String wearId = gameApi.createWear(wear);
            gameApi.sellWear(wearId);
            try {
                Thread.sleep(988);
            } catch (InterruptedException e) {
                System.exit(0);
            }
        }

        return sellingWearFinishTime.stream().min(Long::compareTo).get();
    }

    private long skillAction() throws IOException {
        long[] skillNextTime = gameApi.getSkillsAvailabilityTime();
        final long currentTime = new Date().getTime();
        for (int i = 0; i < 5; i++) {
            final long skillAvTime = skillNextTime[i];
            if (skillAvTime > currentTime) {
                continue;
            }

            // New type (podium) availability check
            if (this.gameApi.isNewTypesAvailable()) {
                String[] availableTypesIds = this.gameApi.getAvailableTypesIds();
                final int typeId = ThreadLocalRandom.current().nextInt(availableTypesIds.length);
                this.gameApi.researchNewType(availableTypesIds[typeId]);
                Main.logger.info("New type successfully researched");
            }

            // New tech availability check
            if (this.gameApi.isNewTechsAvailable()) {
                String[] availableTechIds = this.gameApi.getAvailableTechsIds();
                this.gameApi.researchNewTech(availableTechIds[0]);
                Main.logger.info("New tech was successfully researched");
            }

            try {
                final int taskTimeInc = this.gameApi.doTask(i);
                final int generatedTimeInc = ThreadLocalRandom.current().nextInt(5000, 25000);
                skillNextTime[i] = new Date().getTime() + taskTimeInc + generatedTimeInc;
                Main.logger.info("Skill №" + (i+1) + " was successfully applied!");

                // Simulate gamers reaction time before next skill
                Thread.sleep(ThreadLocalRandom.current().nextInt(1500, 5000));
            } catch (Exception e) {
                Main.logger.error(e);
                System.exit(0);
            }
        }

        int soonSkillNumber = 0;
        for (int i = 1; i < 5; i++) {
            final long skillAvTime = skillNextTime[i];
            if (skillAvTime < skillNextTime[soonSkillNumber] && skillAvTime > currentTime) {
                soonSkillNumber = i;
            }
        }

        long soonSkillAvTime = skillNextTime[soonSkillNumber] - new Date().getTime();
        Main.logger.info("Next skill №"+ (soonSkillNumber+1));
        return soonSkillAvTime;
    }

    private long podiumAction() {
        if (this.gameApi.isPodiumShouldResolve()) {
            try {
                this.gameApi.resolvePodiumStatus();
            } catch (IOException e) {
                System.exit(0);
            }
        }

        if (this.gameApi.isPodiumAvailable()) {
            try {
                this.gameApi.enterPodium();
            } catch (IOException e) {
                System.exit(0);
            }
        }

        return this.gameApi.getPodiumFinishTime();
    }

}
