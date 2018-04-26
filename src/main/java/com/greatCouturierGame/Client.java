package com.greatCouturierGame;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private GameSocketClient gsc;
    private String userKey;
    private int latency;
    private Map<String, String> connectData;

    Client(String uid, String authToken) {
        try {
            final String host = "109.234.153.253";
            final int port = 33333;

            this.gsc = new GameSocketClient(host, port);
            this.sendCommand("SyncTime");

            final Map<String, String> receivedCommands = gsc.receiveData();
            final String syncTimeResponse = receivedCommands.get("SyncTimeResponse");
            if (syncTimeResponse == null) {
                throw new Exception("Sync time error");
            }

            final long serverTime = Long.parseLong(GameSocketClient.getParam(syncTimeResponse, "ServerTime"));
            this.latency = (int) (new Date().getTime() - serverTime);

            final String connectCommandData = "Id:"+ uid +";Pass:"+ authToken +";Friends:;MissionBonus:0";
            this.sendCommand("Connect", connectCommandData);

            receivedCommands.putAll(gsc.receiveData());
            final String connectResponse = receivedCommands.get("ConnectResponse");
            if (connectResponse == null) {
                throw new Exception("Connect error");
            }

            this.userKey = GameSocketClient.getParam(connectResponse, "Key");
            this.connectData = new HashMap<>();
            this.connectData.put("Name", GameSocketClient.getParam(connectResponse, "Name"));
            this.connectData.put("Rating", GameSocketClient.getParam(connectResponse, "Rating"));
            this.connectData.put("SkillData", GameSocketClient.getParam(connectResponse, "PumpRatingCooldowns"));
            this.connectData.put("Dollars", GameSocketClient.getParam(connectResponse, "Dollars"));
            this.connectData.put("NextDollarsTime", GameSocketClient.getParam(connectResponse, "NextDollarsTime"));

            final String topResponse = receivedCommands.get("TopResponse");
            this.connectData.put("Ids", GameSocketClient.getParam(topResponse, "Ids3"));

            if (receivedCommands.containsKey("CanResearchResponse")) {
                final String canResearchResponse = receivedCommands.get("CanResearchResponse");
                this.connectData.put("TechIds", GameSocketClient.getParam(canResearchResponse, "TechIds"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public int getLatency() {
        return this.latency;
    }

    public Map<String, String> getConnectData() {
        return this.connectData;
    }

    public void close() {
        try {
            this.gsc.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void sendCommand(String commandType, String commandData) throws IOException, NoSuchAlgorithmException {
        final String command = "Type:"+ commandType +";"+ commandData;
        final String sigCommand = this.getSignatureOfString(command) + command;

        this.gsc.sendCommand(sigCommand);
    }

    public void sendCommand(String commandType) throws IOException, NoSuchAlgorithmException {
        final String command = "Type:"+ commandType;
        final String sigCommand = this.getSignatureOfString(command) + command;

        this.gsc.sendCommand(sigCommand);
    }

    public Map<String, String> receiveData() {
        return gsc.receiveData();
    }

    private String getSignatureOfString(String commandString) throws NoSuchAlgorithmException {
        StringBuilder signature = new StringBuilder();
        int firstColIndex = commandString.indexOf(":");
        int firstSemicolIndex = commandString.indexOf(";");

        if (firstSemicolIndex == -1) {
            firstSemicolIndex = commandString.length();
        }

        String commandType = commandString.substring(firstColIndex + 1, firstSemicolIndex);

        String hash = "";
        if (!commandType.equals("SyncTime")  && !commandType.equals("Connect")) {
            String salt = getSalt();
            hash = getMD5(commandString + salt);
        }
        signature.insert(0, "Sig:" + hash + ";");

        int commandAndSignatureLength = signature.length() + commandString.length();
        String commandAndSignatureLengthAsString = String.valueOf(commandAndSignatureLength);
        if (commandAndSignatureLengthAsString.length() == 1) {
            commandAndSignatureLengthAsString = "00" + commandAndSignatureLengthAsString;
        } else if (commandAndSignatureLengthAsString.length() == 2) {
            commandAndSignatureLengthAsString = "0" + commandAndSignatureLengthAsString;
        }

        signature.insert(0, "Size:" + commandAndSignatureLengthAsString + ";");
        return signature.toString();
    }
    private String getSalt() throws NoSuchAlgorithmException {
        final String serverKey = "33333";

        return getMD5(this.userKey + serverKey);
    }
    private static String getMD5(String s) throws NoSuchAlgorithmException {
        MessageDigest md5MessageDigest = MessageDigest.getInstance("MD5");
        md5MessageDigest.update(s.getBytes());

        return new BigInteger(1, md5MessageDigest.digest()).toString(16);
    }
}
