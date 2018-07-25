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

    public abstract long doPlayerActions();

    protected long doShopAction() throws IOException {
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
                Bot.simulateHumanReaction();
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
                Bot.simulateHumanReaction();
                logger.info("Sale of clothes "+ clothesId +" successfully started");
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

        return shopData.entrySet().stream()
                .min(shopDataComparator)
                .orElseThrow()
                .getValue();
    }

    protected long doTaskAction() throws IOException, NotConnectedException {
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
            Bot.simulateHumanReaction();
        }

        return Arrays.stream(tasksStatus).min()
                .orElseThrow();
    }

    protected long doPodiumAction() throws IOException, NotConnectedException {
        if (this.gameAPI.isPodiumFinished()) {
            this.gameAPI.finishPodiumContest();
            this.gameAPI.startPodiumContest();
        }

        return this.gameAPI.getPodiumEndTime();
    }

}
