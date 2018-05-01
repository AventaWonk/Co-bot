package com.greatCouturierGame;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Random;

public class GameGateway {

    private GameSocketClient gsc;
    private long latency;
    private String userKey;
    private long[] skillsAvailabilityTime;
    private String[] topShopsIds;
    private String[] availableTechsIds = null;
    private String[] availableTypesIds = null;
    private long podiumFinishTime;
    private static Logger logger = Main.getLogger();

    GameGateway(String uid, String authToken) {
        final String host = "109.234.153.253";
        final int port = 33333;

        this.gsc = new GameSocketClient(host, port);
        try {
            this.syncTimeWithServer();
            this.connectToServer(uid, authToken);
        } catch (IOException e) {
            this.gsc.close();
            logger.fatal(e);
            System.exit(0);
        }
        this.gsc.setUserKey(userKey);
    }

    public void connectToServer(String uid, String authToken) throws IOException {
        final String connectCommandData = "Id:"+ uid +";Pass:"+ authToken +";Friends:;MissionBonus:0";
        this.gsc.sendCommand("Connect", connectCommandData);
        Map<String, String> receivedData = this.gsc.receiveData();
        if (!receivedData.containsKey("ConnectResponse")) {
            throw new IOException("Connect response error");
        }

        final String connectResponse = receivedData.get("ConnectResponse");
        final String topResponse = receivedData.get("TopResponse");

        // Parse data
        final String userKey = GameSocketClient.getParam(connectResponse, "Key");
//        final String name = GameSocketClient.getParam(connectResponse, "Name");
        final String podiumFinishTime = GameSocketClient.getParam(connectResponse, "PodiumFinishTime");
//        final String rating = GameSocketClient.getParam(connectResponse, "Rating");
        final String pumpRatingCooldowns = GameSocketClient.getParam(connectResponse, "PumpRatingCooldowns");
//        final String dollars = GameSocketClient.getParam(connectResponse, "Dollars");
//        final String nextDollarsTime = GameSocketClient.getParam(connectResponse, "NextDollarsTime");
        final String topIds = GameSocketClient.getParam(topResponse, "Ids3");
        String[] availableTechsIds = null;
        String[] availableTypesIds = null;

        if (receivedData.containsKey("CanResearchResponse")) {
            final String canResearchResponse = receivedData.get("CanResearchResponse");
            availableTechsIds = GameSocketClient.getParam(canResearchResponse, "TechIds").split("_");
        }

        if (receivedData.containsKey("CanResearchTypeResponse")) {
            final String canResearchTypeResponse = receivedData.get("CanResearchTypeResponse");
            availableTypesIds = GameSocketClient.getParam(canResearchTypeResponse, "TechIds").split("_");
        }

        // Set connection data fields
        this.userKey = userKey;
        this.topShopsIds = topIds.split("_");
        this.skillsAvailabilityTime = this.parseSkillsAvailableTime(pumpRatingCooldowns);
        this.availableTechsIds = availableTechsIds;
        this.availableTypesIds = availableTypesIds;
        this.podiumFinishTime = Long.parseLong(podiumFinishTime);
    }

    public void syncTimeWithServer() throws IOException {
        this.gsc.sendCommand("SyncTime");
        Map<String, String> receivedData = this.gsc.receiveData();
        if (!receivedData.containsKey("SyncTimeResponse")) {
            throw new IOException("Sync time response error");
        }

        final String syncTimeResponse = receivedData.get("SyncTimeResponse");

        // Parse data
        final long serverTime = Long.parseLong(GameSocketClient.getParam(syncTimeResponse, "ServerTime"));

        this.latency = new Date().getTime() - serverTime;
    }

    public int doTask(int taskNumber) throws InterruptedException, IOException {
        final String selectedTaskId = GameGateway.getTaskIdByNumber(taskNumber);
        final int selectedTaskInc = GameGateway.getTaskIncByNumber(taskNumber);

        Random rnd = new Random(System.currentTimeMillis());
        final String rndTopShopId = this.topShopsIds[rnd.nextInt(this.topShopsIds.length)];
        this.gsc.sendCommand("ShopEnter", "UserId:" + rndTopShopId);
        Map<String, String> shopData = this.gsc.receiveData();
        this.gsc.sendCommand("PumpRating", "ItemId:" + selectedTaskId);
        Map<String, String> responseResultData = this.gsc.receiveData();
        if (!responseResultData.containsKey("PumpRatingResponse")) {
            throw new IOException("Bad response");
        }

        // Simulate gamers reaction time
        Thread.sleep(2121);

        this.gsc.sendCommand("CatchMoney", "Money:1");
        responseResultData.putAll(this.gsc.receiveData());

        if (!responseResultData.containsKey("ConfirmResponse")) {
            throw new IOException("Bad response");
        }

        if (responseResultData.containsKey("CanResearchResponse")) {
            final String canResearchResponse = responseResultData.get("CanResearchResponse");
            this.availableTechsIds = GameSocketClient.getParam(canResearchResponse, "TechIds").split("_");
        }

        if (responseResultData.containsKey("CanResearchTypeResponse")) {
            final String canResearchTypeResponse = responseResultData.get("CanResearchTypeResponse");
            this.availableTypesIds = GameSocketClient.getParam(canResearchTypeResponse, "TechIds").split("_");
        }

//                this.modelRating += taskNumber;

        return selectedTaskInc;
    }

    public void researchNewTech(String techId) {
        try {
            this.gsc.sendCommand("Research", "TechId:" + techId);
            this.availableTechsIds = null;
        } catch (Exception e) {
            logger.error(e);
            System.exit(0);
        }
    }

    public void researchNewType(String techId) {
        try {
            this.gsc.sendCommand("ResearchType", "TechId" + techId);
            this.availableTypesIds = null;
        } catch (Exception e) {
            logger.error(e);
            System.exit(0);
        }
    }

    public void enterPodium() {
        try {
            this.gsc.sendCommand("CheckContest");
            Map<String, String> responseResultData = this.gsc.receiveData();

            if (responseResultData.containsKey("")) {
                this.gsc.sendCommand("SelectPodiumClass", "PodiumClass:1");
                responseResultData = this.gsc.receiveData();
                if (!responseResultData.containsKey("SelectPodiumClassResponse")) {
                    throw new IOException("");
                }
            }

            Random rnd = new Random(System.currentTimeMillis());
            while (responseResultData.containsKey("VotingEnterResponse")) {
                final String votingEnterResponse = responseResultData.get("VotingEnterResponse");
                String[] availableModelsIds = GameSocketClient.getParam(votingEnterResponse, "Ids").split("_");
                String selectedModelId = availableModelsIds[rnd.nextInt(availableModelsIds.length)];
                this.gsc.sendCommand("Vote", "UserId:" + selectedModelId);
                responseResultData = this.gsc.receiveData();

                // Simulate gamers reaction time
                Thread.sleep(988);
            }

            if (!responseResultData.containsKey("CanPodiumResponse")) {
                this.gsc.sendCommand("EnterPodium");
                responseResultData = this.gsc.receiveData();
            }

            if (!responseResultData.containsKey("PodiumStatusResponse")) {
                throw new IOException("");
            }
        } catch (Exception e) {
            logger.fatal(e);
            System.exit(0);
        }
    }

    public void checkPodiumStatus() {
        this.gsc.sendCommand("CheckContest");
        "PodiumStatusResponse"
        this.gsc.sendCommand("EndPodium");
        "ConfirmResponse"
        this.gsc.sendCommand("CheckContest");
        "SelectPodiumClassResponse"
    }

    public boolean isPodiumAvailable() {
        return new Date().getTime() + this.latency > this.podiumFinishTime;
    }


    public boolean isNewTypesAvailable() {
        return this.availableTypesIds != null;
    }

    public String[] getAvailableTypesIds() {
        return availableTypesIds;
    }

    public long[] getSkillsAvailabilityTime() {
        return skillsAvailabilityTime;
    }

    public String[] getTopShopsIds() {
        return topShopsIds;
    }

    public boolean isNewTechsAvailable() {
        return this.availableTechsIds != null;

    }

    public String[] getAvailableTechsIds() {
        return availableTechsIds;
    }

    public void closeGame() {
        this.gsc.close();
    }

//    private void tryToConnect(int count, int delay) throws IOException {
//        for (int i = 0; i < count; i++) {
//
//
//            try {
//                Thread.sleep(delay);
//            } catch (InterruptedException e) {
//                logger.fatal(e);
//                System.exit(0);
//            }
//        }
//
//        throw new IOException("");
//    }

    private long[] parseSkillsAvailableTime(final String skillData) {
        return Arrays.stream(skillData.split("_")).mapToLong(
                (skillAvTime) -> Long.parseLong(skillAvTime) + this.latency
        ).toArray();
    }

    private static String getTaskIdByNumber(int taskNumber) {
        String[] taskIds = {"1", "3", "6", "10", "15"};
        return taskIds[taskNumber];
    }

    private static int getTaskIncByNumber(int taskNumber) {
        int[] taskIncs = {602000, 3602000, 14401000, 28801000, 43201000};
        return taskIncs[taskNumber];
    }
}
