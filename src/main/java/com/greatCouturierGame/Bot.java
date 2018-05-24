package com.greatCouturierGame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Bot implements Runnable {

    private String uid;
    private String authToken;
    private GameGateway gameApi;
    private Task[] tasks;

    Bot(String uid, String authToken) {
        this.uid = uid;
        this.authToken = authToken;
        this.tasks = Task.getAllTasks();
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
        Predicate<Long> isSold = (endTime) -> currentTime > endTime;
        final boolean shouldResolveFlag = sellingWearFinishTime.stream().anyMatch(isSold);
        if (shouldResolveFlag) {
            final String[] soldWeaIds = gameApi.resolveShopStatus();
            for (String wearId : soldWeaIds) {
                gameApi.completeSale(wearId);
                this.simulateHumanReaction();
            }
        }

        while (sellingWearFinishTime.size() < 4) {
            final int[] maxWearTypesIds = this.gameApi.getMaxWearTypesIds();
            final int maxColorId = this.gameApi.getMaxColorId();
            final int maxTextureId = this.gameApi.getMaxTextureId();
            final int maxTextureColorId = this.gameApi.getMaxTextureColorId();
            Wear wear = Wear.generateRandomWear(
                    maxWearTypesIds,
                    maxColorId,
                    maxTextureId,
                    maxTextureColorId
            );
            String wearId = gameApi.createWear(wear);
            gameApi.sellWear(wearId);
            this.simulateHumanReaction();
        }

        return sellingWearFinishTime.stream().min(Long::compareTo).get();
    }

    private long skillAction() throws IOException {
        long[] skillNextTime = gameApi.getSkillsAvailabilityTime();
        final long currentTime = new Date().getTime();

        for (int i = 0; i < this.tasks.length; i++) {
            if (skillNextTime[i] > currentTime) {
                continue;
            }

            // New type availability check
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
                final long taskTimeInc = this.gameApi.doTask(this.tasks[i]);
                final int generatedTimeInc = ThreadLocalRandom.current().nextInt(5000, 25000);
                skillNextTime[i] = System.currentTimeMillis() + taskTimeInc + generatedTimeInc;
                Main.logger.info("Skill №" + (i+1) + " was successfully applied!");
                this.simulateHumanReaction();
            } catch (Exception e) {
                Main.logger.error(e);
                System.exit(0);
            }
        }

        return Stream.of(skillNextTime).min(Long::compareTo).get() - System.currentTimeMillis();
//        int soonSkillNumber = 0;
//        for (int i = 1; i < 5; i++) {
//            final long skillAvTime = skillNextTime[i];
//            if (skillAvTime < skillNextTime[soonSkillNumber] && skillAvTime > currentTime) {
//                soonSkillNumber = i;
//            }
//        }

//        long soonSkillAvTime = skillNextTime[soonSkillNumber] - new Date().getTime();
//        Main.logger.info("Next skill №"+ (soonSkillNumber+1));
//        return soonSkillAvTime;
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

    private void simulateHumanReaction() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
        } catch (InterruptedException e) {

        }
    }

}
