package com.greatCouturierGame.logic;

import com.greatCouturierGame.connection.GameAPI;
import com.greatCouturierGame.connection.NotConnectedException;
import com.greatCouturierGame.data.Task;
import com.greatCouturierGame.data.Wear;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractBotStrategy {

    private static final Logger logger = LogManager.getLogger(AbstractBotStrategy.class);
    private static final Task[] tasks = Task.getAllTasks();

    private GameAPI gameAPI;

    public AbstractBotStrategy(GameAPI gameAPI) {
        this.gameAPI = gameAPI;
    }

    public abstract long doPlayerActions(String uid, String authToken);

    protected long doTaskAction(int taskId) throws IOException, NotConnectedException {
        logger.info("Task action started");
        long[] tasksStatus = gameAPI.getTasksStatus();
        final long currentTime = System.currentTimeMillis();

        for (int i = 0; i < AbstractBotStrategy.tasks.length; i++) {
            if (tasksStatus[i] > currentTime) {
                continue;
            }

            // New type availability check
            String[] availableTypesIds = this.gameAPI.getAvailableTypesIds();
            if (availableTypesIds != null) {
                int typeId = ThreadLocalRandom.current().nextInt(availableTypesIds.length);
                this.gameAPI.researchType(availableTypesIds[typeId]);
                logger.info("New type was successfully researched");
            }

            // New tech availability check
            String[] availableTechIds = this.gameAPI.getAvailableTechsIds();
            if (availableTechIds != null) {
                this.gameAPI.researchTech(availableTechIds[0]);
                logger.info("New tech was successfully researched");
            }

            final long taskTimeInc = this.gameAPI.doTask(AbstractBotStrategy.tasks[i]);
            final int rndTimeInc = ThreadLocalRandom.current().nextInt(1000, 25000);
            tasksStatus[i] = System.currentTimeMillis() + taskTimeInc + rndTimeInc;
            logger.info("Skill №" + (i+1) + " was successfully applied!");
            Player.addDelay();
        }

        logger.info("Task action successfully completed");

        return Arrays.stream(tasksStatus).min()
                .orElseThrow(() -> new IOException("e"));
    }

    protected long doShopAction() throws IOException {
        logger.info("Shop action started");
        final long currentTime = System.currentTimeMillis();
        final Map<String, Long> shopData = gameAPI.getShopStatus();
        final boolean isAnyClothesSold = shopData.entrySet().stream()
                .anyMatch((entry) -> currentTime > entry.getValue());

        // Complete the sale if any clothes was sold
        if (isAnyClothesSold) {
            final String[] soldClothesIds = gameAPI.getSoldClothesIds();
            for (String clothesId : soldClothesIds) {
                gameAPI.completeClothesSell(clothesId);
                logger.info("Clothes "+ clothesId +" was successfully sold");
                Player.addDelay();
            }
        }

        // Create random wear and sell it
        try {
            Wear wear = Wear.generateRandomWear(
                    this.gameAPI.getAvailableClothesIds(),
                    this.gameAPI.getAvailableParametersIds()
            );

            while (shopData.size() < 4) {
                String clothesId = gameAPI.createClothes(wear);
                gameAPI.sellClothes(clothesId);
                Player.addDelay();
                logger.info("Sale of clothes"+ clothesId +" successfully started");
            }
        } catch (Exception e) {
            logger.fatal(e);
        }

        Comparator<Map.Entry<String, Long>> shopDataComparator = (entryA, entryB) -> {
            if (entryA.getValue() > entryB.getValue()) {
                return 1;
            } else if (entryA.getValue() < entryB.getValue()) {
                return -1;
            }

            return 0;
        };

        logger.info("Shop action successfully completed");

        return shopData.entrySet().stream()
                .min(shopDataComparator)
                .orElseThrow(() -> new IOException("e"))
                .getValue();
    }

    protected long doPodiumAction() throws IOException, NotConnectedException {
        if (this.gameAPI.isPodiumFinished()) {
            this.gameAPI.finishPodiumContest();
        }

        if (!this.gameAPI.isPodiumStarted()) {
            this.gameAPI.startPodiumContest();
        }

        return this.gameAPI.getPodiumEndTime();
    }

}
