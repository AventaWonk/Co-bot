package com.greatCouturierGame.adapter;

import com.greatCouturierGame.connection.GameResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class SocketCommunicator implements Communicator {

    private static final Logger logger = LogManager.getLogger(SocketCommunicator.class);
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
        byte[] responseBytes = this.socketService.receive();

        return new GameResponse(responseBytes);
    }

    @Override
    public GameResponse receive(String... responseTypes) throws IOException {
        logger.info("Receiving "+ responseTypes.length +"responses...");
        byte[] tempResponseBytes;
        GameResponse tempResponse;
        byte[] responseBytes = this.socketService.receive();
        GameResponse gameResponse = new GameResponse(responseBytes);
        for (String response : responseTypes) {
            logger.info("Trying to receive response "+ response);
            while (!gameResponse.isContains(response)) {
                tempResponseBytes = this.socketService.receive();
                tempResponse = new GameResponse(tempResponseBytes);
                gameResponse.getResponse().putAll(tempResponse.getResponse());
            }
        }

        return gameResponse;
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

    @Override
    public RequestSigner getRequestSigner() { return this.requestSigner; }

    private void sendRequestBase(String request) throws IOException  {
        if (!this.isConnected()) {
            this.connect();
        }

        final String signedRequest = this.requestSigner.getSign(request) + request;
        this.socketService.send(signedRequest.getBytes("UTF-8"));
    }
}
