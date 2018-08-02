package com.greatCouturierGame.adapter;

import com.greatCouturierGame.connection.GameResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public GameResponse receive(String type) throws IOException {
        logger.info("Receiving "+ type +" response...");
        byte[] responseBytes;
        GameResponse gameResponse = new GameResponse();
        while (!gameResponse.isContains(type)) {
            responseBytes = this.socketService.receive();
            gameResponse.addPart(responseBytes);
        }

        logger.info("Response was successfully received");

        return gameResponse;
    }

    @Override
    public GameResponse receive(String... types) throws IOException {
        logger.info("Receiving "+ types.length +" responses...");
        byte[] responseBytes;
        List<String> typesList = new ArrayList<>(Arrays.asList(types));
        GameResponse gameResponse = new GameResponse();

        while (typesList.size() > 0) {
            responseBytes = this.socketService.receive();
            gameResponse.addPart(responseBytes);
            typesList.removeAll(gameResponse.getResponseMap().keySet());
        }

        logger.info("All responses were successfully received");

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
