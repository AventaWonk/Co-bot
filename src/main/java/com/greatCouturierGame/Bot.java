package com.greatCouturierGame;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

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
            long[] actionsNextTime = new long[3];

            try {
                final long nextPodiumTime = this.doPodiumAction();
                actionsNextTime[0] = nextPodiumTime;
            } catch (IOException e) {
                Main.logger.info("Podium error");
            }

            try {
                final long nextShopTime = this.doShopAction();
                actionsNextTime[1] = nextShopTime;
            } catch (IOException e) {
                Main.logger.info("Shop error");
            }

            try {
                final long nextTaskTime = this.doTaskAction();
                actionsNextTime[2] = nextTaskTime;
            } catch (IOException e) {
                Main.logger.info("Task error");
            }

            this.gameApi.closeGame();
            long soonActionTime = Arrays.stream(actionsNextTime).min().getAsLong();

            try {
                Thread.sleep(soonActionTime);
            } catch (InterruptedException e) {
                System.exit(0);
            }
        }
    }

    private long doShopAction() throws IOException {
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

        return Collections.min(sellingWearFinishTime);
    }

    private long doTaskAction() throws IOException {
        long[] tasksNextTime = gameApi.getTasksAvailabilityTime();
        final long currentTime = System.currentTimeMillis();

        for (int i = 0; i < this.tasks.length; i++) {
            if (tasksNextTime[i] > currentTime) {
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

            final long taskTimeInc = this.gameApi.doTask(this.tasks[i]);
            final int generatedTimeInc = ThreadLocalRandom.current().nextInt(5000, 25000);
            tasksNextTime[i] = System.currentTimeMillis() + taskTimeInc + generatedTimeInc;
            Main.logger.info("Skill №" + (i+1) + " was successfully applied!");
            this.simulateHumanReaction();
        }

        return Arrays.stream(tasksNextTime).min().orElse(0) - System.currentTimeMillis();
    }

    private long doPodiumAction() throws IOException {
        if (this.gameApi.isPodiumShouldResolve()) {
            this.gameApi.resolvePodiumStatus();
        }

        if (this.gameApi.isPodiumAvailable()) {
            this.gameApi.enterPodium();
        }

        return this.gameApi.getPodiumFinishTime();
    }

    private void simulateHumanReaction() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
        } catch (InterruptedException e) {
            System.exit(0);
        }
    }

}
