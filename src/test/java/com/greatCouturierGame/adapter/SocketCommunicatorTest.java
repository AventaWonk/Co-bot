package com.greatCouturierGame.adapter;

import com.greatCouturierGame.connection.GameResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SocketCommunicatorTest {

    private static final char EOL = IOSocketService.EOL;

    @Test
    void receive() throws IOException {
        String serverResponse1 = "Type:TestResponse1;Param:1;"+ EOL;
        String serverResponse2 = "Type:TestResponse2;Param:1;"+ EOL;
        String serverResponse3 = "Type:TestResponse3;Param:1;"+ EOL;
        SocketService mockedSocketService = mock(SocketService.class);
        when(mockedSocketService.receive()).thenReturn(
                serverResponse1.getBytes(),
                serverResponse2.getBytes(),
                serverResponse3.getBytes()
        );

        SocketCommunicator socketCommunicator = new SocketCommunicator(mockedSocketService, null);
        GameResponse actualGameResponse = socketCommunicator.receive("TestResponse3");
        String collectedResponse = serverResponse1 + serverResponse2 + serverResponse3;
        GameResponse expectedGameResponse = new GameResponse(collectedResponse.getBytes());
        assertEquals(expectedGameResponse.getResponse(), actualGameResponse.getResponse());
    }
}