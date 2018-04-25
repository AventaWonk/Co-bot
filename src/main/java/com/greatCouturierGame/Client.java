package com.greatCouturierGame;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private InputStream in;
    private String key = "";
    private int serverLag;
    private Map<String, String> connectData;

    Client(String uid, String authToken) {
        try {
            this.socket = new Socket("109.234.153.253", 33333);
            this.out = new PrintWriter(socket.getOutputStream());
            this.in = socket.getInputStream();

            this.sendCommand("SyncTime");
            Thread.sleep(1000);

            String receivedData = this.receiveData();
            List<String> responseTypes = this.getResponseTypes(receivedData);

            String serverTime = this.getParam(receivedData,"ServerTime");
            this.serverLag = (int) (new Date().getTime() - Long.parseLong(serverTime));

            try {
                String connectData = "Id:"+ uid +";Pass:"+ authToken +";Friends:;MissionBonus:0";
                this.sendCommand("Connect", connectData);
                Thread.sleep(1000);

                receivedData = this.receiveData();
                responseTypes = this.getResponseTypes(receivedData);
                this.key = this.getParam(receivedData,"Key");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }

            this.connectData = new HashMap<>();
            this.connectData.put("Name", this.getParam(receivedData, "Name"));
            this.connectData.put("Rating", this.getParam(receivedData, "Rating"));
            this.connectData.put("SkillData", this.getParam(receivedData, "PumpRatingCooldowns"));
            this.connectData.put("Dollars", this.getParam(receivedData, "Dollars"));
            this.connectData.put("NextDollarsTime", this.getParam(receivedData, "NextDollarsTime"));
            this.connectData.put("Ids", this.getParam(receivedData, "Ids3"));

            if (responseTypes.contains("CanResearchResponse")) {
                this.connectData.put("TechIds", this.getParam(receivedData, "TechIds"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public long getServerTime() {
        return new Date().getTime() + this.serverLag;
    }

    public int getServerLag() {
        return this.serverLag;
    }

    public Map<String, String> getConnectData() {
        return this.connectData;
    }

    public void stop() {
        try {
            this.in.close();
            this.out.close();
            this.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public String receiveData() throws Exception {
        if (!this.socket.isConnected()) {
            throw new Exception("Disconnected");
        }

        int dataSize = 0;
        dataSize = in.available();
        byte byteData[] = new byte[dataSize];
        int receivedSize = in.read(byteData);

        if (receivedSize == 0) {
            throw new Exception("Reader error");
        }

        return new String(byteData);
    }

    public List<String> getResponseTypes(String response) throws Exception {
        List<String> responseTypes = new ArrayList<>();

        String typeTag = "Type:";
        int indTypeTag = 0;
        int indSemicolon = 0;
        while ((indTypeTag = response.indexOf(typeTag, indTypeTag)) != -1) {
            indSemicolon = response.indexOf(";", indSemicolon);
            String type = response.substring(indTypeTag + typeTag.length(), indSemicolon);
            responseTypes.add(type);
        }

        if (responseTypes.size() < 1) {
            throw new Exception("Hasn't types error");
        }

        return responseTypes;
    }

    public String getParam(String response, String param) throws Exception {
        String paramWithCol = param + ":";
        int indParam = response.indexOf(paramWithCol);
        int indSemicolon = response.indexOf(";", indParam);
        if (indParam == -1) {
            throw new Exception("Tag not found! \n Res: "+response+"\n"+param);
        }

        if (indSemicolon == -1 ) {
            return response.substring(indParam + paramWithCol.length());
        }

        return response.substring(indParam + paramWithCol.length(), indSemicolon);
    }

    public void sendCommand(String commandType, String commandData) throws IOException, InterruptedException, NoSuchAlgorithmException {
        String command = "Type:"+ commandType +";"+ commandData;
        String sigCommand = this.getSignatureOfString(command) + command;

        byte[] byteString = sigCommand.getBytes("UTF-8");
        BufferedOutputStream ou = new BufferedOutputStream(this.socket.getOutputStream(), byteString.length + 10);
        ou.write(byteString);
        ou.write((byte) 0x00);
        ou.flush();

        Thread.sleep(200);
    }

    private void sendCommand(String commandType) throws IOException, InterruptedException, NoSuchAlgorithmException {
        String command = "Type:"+ commandType;
        String sigCommand = this.getSignatureOfString(command) + command;

        byte[] byteString = sigCommand.getBytes("UTF-8");
        BufferedOutputStream ou = new BufferedOutputStream(this.socket.getOutputStream(), byteString.length + 10);
        ou.write(byteString);
        ou.write((byte) 0x00);
        ou.flush();

        Thread.sleep(200);
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
        return getMD5(this.key + "33333");
    }
    private static String getMD5(String s) throws NoSuchAlgorithmException {
        MessageDigest md5MessageDigest = MessageDigest.getInstance("MD5");
        md5MessageDigest.update(s.getBytes());

        return new BigInteger(1, md5MessageDigest.digest()).toString(16);
    }
}
