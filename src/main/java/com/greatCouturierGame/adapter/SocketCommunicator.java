package com.greatCouturierGame.adapter;

import com.greatCouturierGame.connection.GameResponse;

import java.io.IOException;

public class SocketCommunicator implements Communicator {

    private static final String GAME_SERVER_HOST = "109.234.153.253";
    private static final int GAME_SERVER_PORT = 33333;

    private SocketService socketService;
    private RequestSigner requestSigner;

    public SocketCommunicator(SocketService socketService, RequestSigner requestSigner) {
        this.socketService = socketService;
        this.requestSigner = requestSigner;
    }

    @Override
    public void send(String type) throws IOException {
        this.sendRequestBase("Type:"+ type);
    }

    @Override
    public void send(String type, String query) throws IOException {
        this.sendRequestBase("Type:"+ type +";"+ query);
    }

    @Override
    public GameResponse receive() throws IOException {
        byte[] bytes = this.socketService.receive();

        return new GameResponse(bytes);
    }

    @Override
    public void connect() throws IOException  {
        this.socketService.connect(
                SocketCommunicator.GAME_SERVER_HOST,
                SocketCommunicator.GAME_SERVER_PORT
        );
    }

    @Override
    public boolean disconnect() {
        try {
            this.socketService.disconnect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isConnected() {
        return this.socketService.isConnected();
    }

    private void sendRequestBase(String request) throws IOException  {
        final String signedRequest = this.requestSigner.getSign(request) + request;
        this.socketService.send(signedRequest.getBytes("UTF-8"));
    }
}
