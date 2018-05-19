package com.greatCouturierGame;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameGateway {

    private String uid;
    private GameSocketClient gsc;
    private long latency;
    private String userKey;
    private long[] skillsAvailabilityTime;
    private String[] topShopsIds;
    private String[] availableTechsIds = null;
    private String[] availableTypesIds = null;
    private Set<Integer> maxWearTypesIds = new HashSet<>();
    private List<String> sellingWearIds;
    private List<Long> sellingWearEndTime;
    private Set<String> wardrobeWearIds;
    private int maxTextureId;
    private int maxColorId;
    private int maxTextureColorId;
    private int nextWearId = 0;
    private long podiumFinishTime;
    private boolean podiumFinishFlag;

    GameGateway(String uid, String authToken) {
        final String host = "109.234.153.253";
        final int port = 33333;
        this.uid = uid;
        this.gsc = new GameSocketClient(host, port);
        try {
            this.syncTimeWithServer();
            this.connectToServer(uid, authToken);
            this.gsc.setUserKey(userKey);
        } catch (IOException e) {
            this.gsc.close();
            Main.logger.fatal(e);
            System.exit(0);
        }
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
        this.userKey = GameSocketClient.getParam(connectResponse, "Key");
        final String[] sellingWearIds = GameSocketClient.getParam(connectResponse, "SellWearIds").split("_");
        final String[] wardrobeWearIds = GameSocketClient.getParam(connectResponse, "WearIds").split("_");
        final String nextWearId = GameSocketClient.getParam(connectResponse, "WearNextId");
        final String[] sellingWearEndTime = GameSocketClient.getParam(connectResponse, "SellWearTimes").split("_");
        final String podiumFinishTime = GameSocketClient.getParam(connectResponse, "PodiumFinishTime");
        final String podiumFinishFlag = GameSocketClient.getParam(connectResponse, "BodyPodiumWearIds");
        final String[] pumpRatingCooldowns = GameSocketClient.getParam(connectResponse, "PumpRatingCooldowns").split("_");
        this.topShopsIds = GameSocketClient.getParam(topResponse, "Ids3").split("_");
        final String[] wearData = GameSocketClient.getParam(connectResponse, "AvailableComponents").split("_");
        final int[] availableComponentsMaxTypes = Stream.of(wearData).mapToInt(Integer::parseInt).toArray();
        for (int id : availableComponentsMaxTypes) {
            switch (id / 100) {
                case 2:
                    this.maxTextureId = id;
                    break;
                case 3:
                    this.maxColorId = id;
                    break;
                case 8:
                    this.maxTextureColorId = id;
                    break;
                default:
                    if (id < 800 && id > 400) {
                        this.maxWearTypesIds.add(id);
                    }
                    break;
            }
        }

        this.checkNewSkillAv(receivedData);

        // Set connection data fields
        this.nextWearId = Integer.parseInt(nextWearId);
        this.sellingWearEndTime = Arrays.stream(this.parseServerTimeData(sellingWearEndTime)).boxed().collect(Collectors.toList());
        this.sellingWearIds = Arrays.stream(sellingWearIds).collect(Collectors.toList());
        this.wardrobeWearIds = Arrays.stream(wardrobeWearIds).collect(Collectors.toSet());
        this.skillsAvailabilityTime = this.parseServerTimeData(pumpRatingCooldowns);
        this.podiumFinishTime = Long.parseLong(podiumFinishTime);
        this.podiumFinishFlag = podiumFinishFlag.isEmpty();
    }

    public void syncTimeWithServer() throws IOException {
        this.gsc.sendCommand("SyncTime");
        Map<String, String> receivedData = this.gsc.receiveData();
        if (!receivedData.containsKey("SyncTimeResponse")) {
            throw new IOException("Sync time response error");
        }

        final String syncTimeResponse = receivedData.get("SyncTimeResponse");
        final long serverTime = Long.parseLong(GameSocketClient.getParam(syncTimeResponse, "ServerTime"));
        this.latency = new Date().getTime() - serverTime;
    }

    public int doTask(int taskNumber) throws IOException {
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
        try {
            Thread.sleep(2121);
        } catch (InterruptedException e) {
            System.exit(0);
        }

        this.gsc.sendCommand("CatchMoney", "Money:1");
        responseResultData.putAll(this.gsc.receiveData());

        if (!responseResultData.containsKey("ConfirmResponse")) {
            throw new IOException("Bad response");
        }

        checkNewSkillAv(responseResultData);

        return selectedTaskInc;
    }

    public void researchNewTech(String techId) throws IOException {
        this.gsc.sendCommand("Research", "TechId:" + techId);
        this.availableTechsIds = null;
    }

    public void researchNewType(String techId) throws IOException {
        this.gsc.sendCommand("ResearchType", "TechId" + techId);
        this.availableTypesIds = null;
    }

    public void resolvePodiumStatus() throws IOException {
        this.gsc.sendCommand("CheckContest");
        Map <String, String> responseResultData = this.gsc.receiveData();
        if (!responseResultData.containsKey("PodiumStatusResponse")) {
            return;
        }

        final String podiumStatusResponse = responseResultData.get("PodiumStatusResponse");
        final String[] ids = GameSocketClient.getParam(podiumStatusResponse, "Ids").split("_");
        int gamerPosition = Arrays.binarySearch(ids, this.uid);
        String place = "";
        if (gamerPosition >= 0) {
            place = GameSocketClient.getParam(podiumStatusResponse, "Places").split("_")[gamerPosition];
        }

        Long podiumFinishTime =  Long.parseLong(GameSocketClient.getParam(podiumStatusResponse, "PodiumFinishTime"));
        if (podiumFinishTime == -1 || podiumFinishTime > new Date().getTime()) {
            return;
        }

        this.gsc.sendCommand("EndPodium");
        responseResultData = this.gsc.receiveData();
        if (!responseResultData.containsKey("ConfirmResponse")) {
            throw new IOException("");
        }

        this.podiumFinishFlag = true;
        this.podiumFinishTime = -1;

        Main.logger.info("Podium was successfully ended! Place:" + place);
    }

    public void enterPodium() throws IOException {
        this.gsc.sendCommand("CheckContest");
        Map<String, String> responseResultData = this.gsc.receiveData();

        if (responseResultData.containsKey("SelectPodiumClassResponse")) {
            this.gsc.sendCommand("SelectPodiumClass", "PodiumClass:1");
            responseResultData = this.gsc.receiveData();
        }

        Random rnd = new Random(System.currentTimeMillis());
        while (responseResultData.containsKey("VotingEnterResponse")) {
            final String votingEnterResponse = responseResultData.get("VotingEnterResponse");
            String[] availableModelsIds = GameSocketClient.getParam(votingEnterResponse, "Ids").split("_");
            String selectedModelId = availableModelsIds[rnd.nextInt(availableModelsIds.length)];
            this.gsc.sendCommand("Vote", "UserId:" + selectedModelId);
            responseResultData = this.gsc.receiveData();

            // Simulate gamers reaction time
            try {
                Thread.sleep(988);
            } catch (InterruptedException e) {
                System.exit(0);
            }
        }

        if (responseResultData.containsKey("CanPodiumResponse")) {
            this.gsc.sendCommand("EnterPodium");
            responseResultData = this.gsc.receiveData();
        }

        if (!responseResultData.containsKey("PodiumStatusResponse")) {
            throw new IOException("");
        }

        this.podiumFinishFlag = false;
        this.podiumFinishTime = System.currentTimeMillis() + 4*60*60 + this.latency;

        Main.logger.info("Podium was successfully entered!");
    }

    public String[] resolveShopStatus() throws IOException {
        this.gsc.sendCommand("ShopSellStatus");
        Map <String, String> responseResultData = this.gsc.receiveData();
        if (!responseResultData.containsKey("ShopSellStatusResponse")) {
            throw new IOException("");
        }

        final String shopSellStatusResponse = responseResultData.get("ShopSellStatusResponse");
//        String[] soldWearCosts = GameSocketClient.getParam(shopSellStatusResponse, "SellWearCosts").split("_");
        final String soldWearIdsParam = GameSocketClient.getParam(shopSellStatusResponse, "SellWearIds");
        if (soldWearIdsParam.isEmpty()) {
            return null;
        }

        return soldWearIdsParam.split("_");
    }

    public void completeSale(String wearId) throws IOException {
        this.gsc.sendCommand("ShopSoldWear", "WearId:"+ wearId);
        Map <String, String> responseResultData = this.gsc.receiveData();
        if (!responseResultData.containsKey("ConfirmResponse")) {
            throw new IOException("");
        }

        int i = this.sellingWearIds.indexOf(wearId);
        this.sellingWearIds.remove(i);
        this.sellingWearEndTime.remove(i);
    }

    public String createWear(Wear wear) throws IOException {
        String commandData = "WearId:"+ this.nextWearId +";WearType:"+ wear.getWearType()
                +";WearColor:"+ wear.getWearColor() +";WearTexture:"+ wear.getWearTexture()
                +";WearTextureColor:"+ wear.getWearTextureColor() +";WearTextureParams:;WearTexture2:"+ wear.getWearTexture2()
                +";WearTextureColor2:"+ wear.getWearTextureColor2() +";WearTextureParams2:";
        this.gsc.sendCommand("CreateWear", commandData);
        Map<String, String> responseResultData = this.gsc.receiveData();
        if (!responseResultData.containsKey("ConfirmResponse")) {
            throw new IOException("");
        }

        this.nextWearId++;
        return String.valueOf(this.nextWearId - 1);
    }

    public void sellWear(String wearId) throws IOException {
        this.gsc.sendCommand("ShopAddWear", "WearId:"+ wearId +";TimeHour:3;Password:");
        Map<String, String> responseResultData = this.gsc.receiveData();
        if (!responseResultData.containsKey("ConfirmResponse")) {
            throw new IOException("");
        }

        this.sellingWearIds.add(wearId);
        this.sellingWearEndTime.add(System.currentTimeMillis() + 3 * 60 * 60 * 1000);
    }

    public boolean isPodiumAvailable() {
        return this.podiumFinishFlag && this.podiumFinishTime == -1;
    }

    public boolean isPodiumShouldResolve() {
        return this.podiumFinishTime != -1 && this.podiumFinishTime < new Date().getTime();
    }

    public long getPodiumFinishTime() {
        return podiumFinishTime;
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

    public int[] getMaxWearTypesIds() {
        return maxWearTypesIds.stream().mapToInt(Integer::intValue).toArray();
    }

    public int getMaxTextureId() {
        return maxTextureId;
    }

    public int getMaxColorId() {
        return maxColorId;
    }

    public int getMaxTextureColorId() {
        return maxTextureColorId;
    }

    public List<Long> getSellingWearEndTime() {
        return sellingWearEndTime;
    }

    public List<String> getSellingWearIds() {
        return sellingWearIds;
    }

    public Set<String> getWardrobeWearIds() {
        return wardrobeWearIds;
    }

    public void closeGame() {
        this.gsc.close();
    }

    private void checkNewSkillAv(Map<String, String> responseResultData) {
        if (responseResultData.containsKey("CanResearchResponse")) {
            final String canResearchResponse = responseResultData.get("CanResearchResponse");
            this.availableTechsIds = GameSocketClient.getParam(canResearchResponse, "TechIds").split("_");
        }

        if (responseResultData.containsKey("CanResearchTypeResponse")) {
            final String canResearchTypeResponse = responseResultData.get("CanResearchTypeResponse");
            this.availableTypesIds = GameSocketClient.getParam(canResearchTypeResponse, "TechIds").split("_");
        }
    }

    private long[] parseServerTimeData(final String[] serverTime) {
        return Arrays.stream(serverTime).mapToLong(
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
