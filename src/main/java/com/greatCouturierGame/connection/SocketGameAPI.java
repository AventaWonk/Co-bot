package com.greatCouturierGame.connection;

import com.greatCouturierGame.adapter.Communicator;
import com.greatCouturierGame.data.Task;
import com.greatCouturierGame.data.Wear;
import com.greatCouturierGame.logic.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SocketGameAPI implements GameAPI {

    private static final Logger logger = LogManager.getLogger(SocketGameAPI.class);

    private String uid;
    private Communicator communicator;

    protected long latency;
    private long[] skillsAvailabilityTime;
    private String[] shopsIds;

    private Map<String, Long> shopData;
    private Map<Wear.Parameters, Integer> clothesParametersIds;
    private List<Integer> clothesIds;
    private int nextClothesId;

    private long podiumFinishTime;
    private boolean podiumStartedFlag;

    private String[] availableTechsIds;
    private String[] availableTypesIds;

    public SocketGameAPI(Communicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public void connect(String uid, String authToken) throws IOException, WrongCredentialsException {
        this.uid = uid;
        GameResponse connectResponse;
        logger.info("Try to connect...");
        try {
            this.communicator.send(
                    "Connect",
                    "Id:"+ uid +";Pass:"+ authToken +";Friends:;MissionBonus:0"
            );
            connectResponse =  this.communicator.receive()
                    .shouldContain("ConnectResponse")
                    .shouldContain("TopResponse");
        } catch (IOException e) {
            throw new WrongCredentialsException("Wrong uid or auth token", e);
        }

        logger.info("The connect response successfully received");
        Map<String, String> connectData = connectResponse.getQuery("ConnectResponse")
                .getParameters(
                        "Key",
                        "WearIds",
                        "WearNextId",
                        "SellWearIds",
                        "SellWearTimes",
                        "PodiumFinishTime",
                        "BodyPodiumWearIds",
                        "PumpRatingCooldowns",
                        "AvailableComponents"
                );

        Map<String, String> topsData =  connectResponse.shouldContain("TopResponse")
                .getQuery("TopResponse")
                .getParameters(
                        "Ids3"
                );

        this.setShopData(
                connectData.get("SellWearIds").split("_"),
                connectData.get("SellWearTimes").split("_")
        );
        this.setClothesData(
                connectData.get("AvailableComponents").split("_")
        );
        this.setSkillsTime(
                connectData.get("PumpRatingCooldowns").split("_")
        );
        this.shopsIds = topsData.get("Ids3").split("_");
        this.nextClothesId = Integer.parseInt(connectData.get("WearNextId"));
        this.podiumFinishTime = Long.parseLong(connectData.get("PodiumFinishTime"));
        this.podiumStartedFlag = !connectData.get("BodyPodiumWearIds").isEmpty();
        String key = connectData.get("Key");
        if (key == null || key.isEmpty()) {
            throw new IOException("Wrong key");
        }

        logger.info("Key parsed: "+ key);
        this.communicator.getRequestSigner().setUserKey(key);
        this.checkResearches(connectResponse);
    }

    @Override
    public void disconnect() {
        this.communicator.disconnect();
    }

    @Override
    public void syncTimeWithServer() throws IOException {
        logger.info("Try to synchronize time with server...");
        this.communicator.send("SyncTime");
        String serverTime  = this.communicator.receive().shouldContain("SyncTimeResponse")
                .getQuery("SyncTimeResponse")
                .getParameter("ServerTime");

        this.latency = System.currentTimeMillis() - Long.valueOf(serverTime);
        logger.info("Latency: "+ this.latency);
    }

    @Override
    public long[] getTasksStatus() throws NotConnectedException {
        return skillsAvailabilityTime;
    }

    @Override
    public long doTask(Task task) throws IOException {
        logger.info("Try to do task...");
        final int rndShopIdIndex = ThreadLocalRandom.current().nextInt(this.shopsIds.length);
        final String rndShopId = this.shopsIds[rndShopIdIndex];
        this.communicator.send("ShopEnter", "UserId:" + rndShopId);
        this.communicator.receive("ShopEnterResponse");
        this.communicator.send("PumpRating", "ItemId:" + task.getId());
        this.communicator.receive("PumpRatingResponse");
        this.communicator.send("CatchMoney", "Money:1");
        GameResponse confirmResponse = this.communicator.receive("ConfirmResponse");
        this.checkResearches(confirmResponse);
        logger.info("Done");

        return task.getCooldown();
    }

    @Override
    public String createClothes(Wear wear) throws IOException {
        logger.info("Try to create clothes...");
        String commandData = "WearId:"+ this.nextClothesId +";WearType:"+ wear.getWearType()
                +";WearColor:"+ wear.getWearColor() +";WearTexture:"+ wear.getWearTexture()
                +";WearTextureColor:"+ wear.getWearTextureColor() +";WearTextureParams:;WearTexture2:"+ wear.getWearTexture2()
                +";WearTextureColor2:"+ wear.getWearTextureColor2() +";WearTextureParams2:";
        this.communicator.send("CreateWear", commandData);
        this.communicator.receive("ConfirmResponse");
        this.nextClothesId++;

        return String.valueOf(this.nextClothesId - 1);
    }

    @Override
    public void sellClothes(String clothesId) throws IOException {
        logger.info("Try to sell clothes...");
        this.communicator.send("ShopAddWear", "WearId:"+ clothesId +";TimeHour:3;Password:");
        this.communicator.receive("ConfirmResponse");
        this.shopData.put(clothesId, System.currentTimeMillis() + 3*60*60*1000);
        logger.info("Sale of "+ clothesId +" starts");
    }

    public String[] getSoldClothesIds() throws IOException {
        this.communicator.send("ShopSellStatus");
        String sellWearIds = this.communicator.receive("ShopSellStatusResponse")
                .getQuery("ShopSellStatusResponse")
                .getParameter("SellWearIds");

        if (sellWearIds == null || sellWearIds.isEmpty()) {
            return null;
        }

        return sellWearIds.split("_");
    }

    @Override
    public void completeClothesSell(String clothesId) throws IOException {
        logger.info("Try to complete sell...");
        this.communicator.send("ShopSoldWear", "WearId:"+ clothesId);
        this.communicator.receive("ConfirmResponse");
        this.shopData.remove(clothesId);
        logger.info("Sale of "+ clothesId +" is complete");
    }

    @Override
    public boolean isPodiumStarted() {
        return this.podiumStartedFlag && this.podiumFinishTime != -1;
    }

    @Override
    public boolean isPodiumFinished() {
        return this.podiumFinishTime != -1 && this.podiumFinishTime < System.currentTimeMillis();
    }

    @Override
    public void startPodiumContest() throws IOException {
        logger.info("Try to start new podium contest...");
        this.communicator.send("CheckContest");
        GameResponse contestResponse = this.communicator.receive();
        if (contestResponse.isContains("SelectPodiumClassResponse")) {
            this.communicator.send("SelectPodiumClass", "PodiumClass:1");
            contestResponse = this.communicator.receive();
        }

        while (contestResponse.isContains("VotingEnterResponse")) {
            final String[] availableIds = contestResponse.getQuery("VotingEnterResponse")
                    .getParameter("Ids")
                    .split("_");

            String randomId = availableIds[ThreadLocalRandom.current().nextInt(availableIds.length)];
            this.communicator.send("Vote", "UserId:" + randomId);
            contestResponse = this.communicator.receive();
            Player.addDelay();
        }

        if (contestResponse.isContains("CanPodiumResponse")) {
            this.communicator.send("EnterPodium");
            contestResponse = this.communicator.receive();
        }

        contestResponse.shouldContain("PodiumStatusResponse");
        this.podiumStartedFlag = true;
        this.podiumFinishTime = System.currentTimeMillis() + 3*60*60 + this.latency;
        logger.info("Done");
    }

    @Override
    public void finishPodiumContest() throws IOException {
        logger.info("Try to finish podium contest...");
        this.communicator.send("CheckContest");
        QueryParser podiumStatusQuery = this.communicator.receive()
                .getQuery("PodiumStatusResponse");
        if (podiumStatusQuery == null) {
            return;
        }

        final String[] ids = podiumStatusQuery.getParameter("Ids")
                .split("_");

        // find index of uid in ids array
        int gamerPosition = 0;
        for (int i = 0; i < ids.length; i++) {
            if (this.uid.equals(ids[i])) {
                gamerPosition = i;
                break;
            }
        }

        String place = podiumStatusQuery.getParameter("Places")
                .split("_")[gamerPosition];

        String podiumFinishTime = podiumStatusQuery.getParameter("PodiumFinishTime");
        if (podiumFinishTime.equals("-1") || Long.valueOf(podiumFinishTime) > System.currentTimeMillis()) {
            return;
        }

        this.communicator.send("EndPodium");
        this.communicator.receive("ConfirmResponse");

        this.podiumStartedFlag = false;
        this.podiumFinishTime = -1;

        logger.info("Podium was successfully ended! Place:" + place);
    }

    @Override
    public void researchTech(String techId) throws IOException {
        this.communicator.send("Research", "TechId:" + techId);
        this.communicator.receive("ConfirmResponse");
        this.availableTechsIds = null;
    }

    @Override
    public void researchType(String techId) throws IOException {
        this.communicator.send("ResearchType", "TechId" + techId);
        this.communicator.receive("ConfirmResponse");
        this.availableTypesIds = null;
    }

    @Override
    public Map<String, Long> getShopStatus() {
        return shopData;
    }

    @Override
    public Map<Wear.Parameters, Integer> getAvailableParametersIds() {
        return clothesParametersIds;
    }

    @Override
    public List<Integer> getAvailableClothesIds() {
        return clothesIds;
    }

    @Override
    public long getPodiumEndTime() {
        return podiumFinishTime;
    }

    public String[] getAvailableTypesIds() {
        return availableTypesIds;
    }

    public long[] getTasksAvailabilityTime() {
        return skillsAvailabilityTime;
    }

    public String[] getAvailableTechsIds() {
        return availableTechsIds;
    }

    protected void setClothesData(String[] clothesData) {
        List<Integer> clothesIds = new LinkedList<>();
        Map<Wear.Parameters, Integer> clothesParametersIds = new HashMap<>();
        for (String sid : clothesData) {
            int id = Integer.parseInt(sid);
            switch (id / 100) {
                case 2:
                    clothesParametersIds.put(Wear.Parameters.TEXTURE, id);
                    break;
                case 3:
                    clothesParametersIds.put(Wear.Parameters.COLOR, id);
                    break;
                case 8:
                    clothesParametersIds.put(Wear.Parameters.TEXTURE_COLOR, id);
                    break;
                default:

                    if (id < 800 && id > 400) {
                        clothesIds.add(id);
                    }

                    break;
            }
        }

        this.clothesIds = clothesIds;
        this.clothesParametersIds = clothesParametersIds;
    }

    protected void setShopData(String[] wearIds, String[] endTime) {
        Map<String, Long> shopData = new HashMap<>(wearIds.length);
        for (int i = 0; i < wearIds.length; i++) {
            shopData.put(
                    wearIds[i],
                    Long.valueOf(endTime[i])
            );
        }

        this.shopData = shopData;
    }

    protected void setSkillsTime(String[] availabilityTime) {
        this.skillsAvailabilityTime = Arrays.stream(availabilityTime)
                .mapToLong(Long::valueOf)
                .map((serverTime) -> serverTime + this.latency)
                .toArray();
    }


    private void checkResearches(GameResponse response) throws IOException {
        if (response.isContains("CanResearchResponse")) {
            this.availableTechsIds = response.getQuery("CanResearchResponse")
                    .getParameter("TechIds")
                    .split("_");
        }

        if (response.isContains("CanResearchTypeResponse")) {
            this.availableTypesIds = response.getQuery("CanResearchTypeResponse")
                    .getParameter("TechIds")
                    .split("_");
        }
    }

}
