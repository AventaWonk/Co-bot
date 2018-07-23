package com.greatCouturierGame.adapter;

import java.io.IOException;

public interface SocketService {
    void connect(String ip, int port) throws IOException;

    void disconnect() throws IOException;

    void send(byte[] request) throws IOException;

    byte[] receive() throws IOException;

    boolean isConnected();
}
