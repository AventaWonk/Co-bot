package com.greatCouturierGame.connection;

import com.greatCouturierGame.adapter.IOSocketService;
import com.greatCouturierGame.adapter.RequestSigner;
import com.greatCouturierGame.adapter.SocketCommunicator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SocketGameAPITest {

    @Test
    void connect() throws Exception {
        byte delimiter = 0x00;
        SocketCommunicator mockedCommunicator = mock(SocketCommunicator.class);
        String connectResponseStr = "Type:ConnectResponse;NextDollarsTime:1530000000000;" +
                "PumpRatingCooldowns:1525000000000_1525000000001_1525000000002_1525000000003_1525000000004_0;" +
                "AvailableComponents:209_409;WearNextId:1;WearIds:11_12;" +
                "WearTypes:409_410;WearColors:309_310;WearTextures:201_200_200;WearTextureColors:802_801;" +
                "SellWearIds:1_2;SellWearTimes:1525000000000_1526000000000;PodiumFinishTime:1525000000000;" +
                "BodyPodiumWearIds:1_2;SkinColor:1;SellWearPasswords:;Key:1525000000000;"+ IOSocketService.EOL +
                "Type:TopResponse;Ids3:123321_123321;"+ IOSocketService.EOL;

        GameResponse connectGameResponse = new GameResponse(connectResponseStr.getBytes());
        when(mockedCommunicator.receive()).thenReturn(connectGameResponse);

        // When set user key (bad)
        RequestSigner mockedRequestSigner = mock(RequestSigner.class);
        when(mockedCommunicator.getRequestSigner()).thenReturn(mockedRequestSigner);

        SocketGameAPI gameAPI = new SocketGameAPI(mockedCommunicator);
        gameAPI.connect("test", "test");

        // Check clothes ids parsing
        List<Integer> actualClothesIds = gameAPI.getAvailableClothesIds();
        assertEquals(1, actualClothesIds.size());
        assertEquals(409, actualClothesIds.get(0).intValue());

        // Check tasks availability time parsing
        long[] actualAvailabilityTime = gameAPI.getTasksAvailabilityTime();
        assertEquals(1525000000000L, actualAvailabilityTime[0]);
        assertEquals(1525000000003L, actualAvailabilityTime[3]);

        // Check shop status parsing
        Map<String, Long> actualShopStatus = gameAPI.getShopStatus();
        assertEquals(2, actualShopStatus.size());
        assertEquals(1525000000000L, actualShopStatus.get("1").longValue());
    }

    @Test
    void syncTimeWithServer() throws IOException {
        SocketCommunicator mockedCommunicator = mock(SocketCommunicator.class);
        String syncTimeResponseStr = "Type:SyncTimeResponse;ServerTime:"+ System.currentTimeMillis() +";"+ IOSocketService.EOL;

        GameResponse syncGameResponse = new GameResponse(syncTimeResponseStr.getBytes());
        when(mockedCommunicator.receive()).thenReturn(syncGameResponse);

        SocketGameAPI gameAPI = new SocketGameAPI(mockedCommunicator);
        gameAPI.syncTimeWithServer();
        long latency = gameAPI.latency;
        assertTrue(() -> latency < 10000);
    }

}