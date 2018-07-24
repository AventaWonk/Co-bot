package com.greatCouturierGame.connection;

import com.greatCouturierGame.data.Task;
import com.greatCouturierGame.data.Wear;

import java.io.IOException;
import java.util.Map;

public interface GameAPI {
    void connect(String uid, String authToken) throws WrongCredentialsException, IOException;

    void disconnect() throws IOException;

    void syncTimeWithServer() throws IOException;

    long[] getTasksStatus() throws NotConnectedException;

    long doTask(Task task)  throws IOException;

    String createClothes(Wear wear) throws IOException;

    void sellClothes(String clothesId) throws IOException;

    void completeClothesSell(String clothesId) throws IOException;

    Map<String, Long> getShopStatus() throws IOException;

    String[] getSoldClothesIds() throws IOException;

    boolean isPodiumFinished() throws NotConnectedException;

    long getPodiumEndTime() throws NotConnectedException;

    void startPodiumContest() throws IOException;

    void finishPodiumContest() throws IOException;

    String[] getAvailableTypesIds() throws NotConnectedException;

    String[] getAvailableTechsIds() throws NotConnectedException;

    void researchType(String typeId) throws IOException;

    void researchTech(String typeId) throws IOException;
}
