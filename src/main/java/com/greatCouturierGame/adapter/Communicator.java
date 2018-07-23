package com.greatCouturierGame.adapter;

import com.greatCouturierGame.connection.GameResponse;

import java.io.IOException;

public interface Communicator {
    void send(String type) throws IOException;

    void send(String type, String query)  throws IOException;

    GameResponse receive() throws IOException;

    void connect() throws IOException;

    boolean disconnect();

    boolean isConnected();
}
