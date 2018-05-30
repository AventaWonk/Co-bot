package com.greatCouturierGame;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class GameSocketClient {

    private static final byte EOR = (byte) 0x00;

    private Socket sc;
    private InputStream in;
    private BufferedOutputStream out;
    private final String serverKey = "33333";
    private String userKey;

    GameSocketClient(String hostname, int port) {
        try {
            this.sc = new Socket(hostname, port);
            this.sc.setSoTimeout(1000);
            this.in = sc.getInputStream();
            this.out = new BufferedOutputStream(sc.getOutputStream());
        } catch (IOException e) {
            System.exit(0);
        }
    }

    public void sendCommand(String commandType, String commandData) throws IOException {
        this.sendCommandBase("Type:"+ commandType +";"+ commandData);
    }

    public void sendCommand(String commandType) throws IOException {
        this.sendCommandBase("Type:"+ commandType);
    }

    public Map<String, String> receiveData() throws IOException {
        Map<String, String> receivedCommands = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        try {
            int lagCount = 0;
            while (this.in.available() == 0) {
                Thread.sleep(100);
                lagCount++;

                if (lagCount > 30) {
                    throw new IOException("Server is not responding now");
                }
            }

            int data;
            while ((data = this.in.read()) != -1) {
                sb.append((char) data);

                if ((byte) data == GameSocketClient.EOR) {
                    String response = sb.toString();
                    sb = new StringBuilder();

                    try {
                        final String responseType = getResponseType(response);
                        receivedCommands.put(responseType, response);
                    } catch (NullPointerException e) {
                        Main.logger.error(e);
                    }
                }
            }
        }  catch (SocketTimeoutException e) {

        }  catch (InterruptedException e) {
            System.exit(0);
        }

        return receivedCommands;
    }

    public void close() {
        try {
            this.in.close();
            this.out.close();
            this.sc.close();
        } catch (IOException e) {
            Main.logger.error(e);
            System.exit(0);
        }
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }
    private void sendCommandBase(String command) throws IOException {
        final String signedCommand = this.getSignatureOfString(command) + command;
        byte[] bt = signedCommand.getBytes("UTF-8");
        this.out.write(bt);
        this.out.write(GameSocketClient.EOR);
        this.out.flush();
    }
    public static String getParam(String response, String param) throws NullPointerException {
        String paramWithCol = param + ":";
        int indParam = response.indexOf(paramWithCol);
        int indSemicolon = response.indexOf(";", indParam);
        if (indParam == -1) {
            throw new NullPointerException("Tag not found! \n Res: "+response+"\n"+param);
        }

        if (indSemicolon == -1 ) {
            return response.substring(indParam + paramWithCol.length());
        }

        return response.substring(indParam + paramWithCol.length(), indSemicolon);
    }
    protected static String getResponseType(String response) throws NullPointerException {
        String typeTag = "Type:";
        int indTypeTag = response.indexOf(typeTag);
        int indSemicolon = response.indexOf(";", indTypeTag);
        String type = response.substring(indTypeTag + typeTag.length(), indSemicolon);

        if (type.length() < 1) {
            throw new NullPointerException("Hasn't types error");
        }

        return type;
    }
    private String getSignatureOfString(String commandString) {
        StringBuilder signature = new StringBuilder();
        int firstColIndex = commandString.indexOf(":");
        int firstSemicolIndex = commandString.indexOf(";");

        if (firstSemicolIndex == -1) {
            firstSemicolIndex = commandString.length();
        }

        String commandType = commandString.substring(firstColIndex + 1, firstSemicolIndex);

        String hash = "";
        String salt;
        if (!commandType.equals("SyncTime")  && !commandType.equals("Connect")) {
            try {
                salt = getSalt();
                hash = getMD5(commandString + salt);
            } catch (NoSuchAlgorithmException e) {
                System.exit(0);
            }
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
        return getMD5(this.userKey + this.serverKey);
    }
    private static String getMD5(String s) throws NoSuchAlgorithmException {
        MessageDigest md5MessageDigest = MessageDigest.getInstance("MD5");
        md5MessageDigest.update(s.getBytes());
        String md5 = new BigInteger(1, md5MessageDigest.digest()).toString(16);

        while (md5.length() < 32) {
            md5 = "0" + md5;
        }

        return md5;
    }
}


