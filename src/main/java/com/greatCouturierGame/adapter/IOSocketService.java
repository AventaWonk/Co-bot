package com.greatCouturierGame.adapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class IOSocketService implements SocketService {

    public static final byte EOL = 0x00;

    private static final Logger logger = LogManager.getLogger(IOSocketService.class);

    private Socket sc;
    private BufferedInputStream in;
    private BufferedOutputStream out;

    public IOSocketService() {
        this.sc = new Socket();
        try {
            this.sc.setSoTimeout(1000);
        } catch (SocketException e) {
            logger.fatal(e);
            System.exit(1);
        }
    }

    @Override
    public void connect(String ip, int port) throws IOException {
        this.sc.connect(new InetSocketAddress(ip, port));
        this.in = new BufferedInputStream(this.sc.getInputStream(), 1024);
        this.out = new BufferedOutputStream(this.sc.getOutputStream(), 1024);
        logger.info("Connected to "+ ip);
    }

    @Override
    public void disconnect() throws IOException {
        this.sc.close();
        logger.info("Disconnected");
    }

    @Override
    public void send(byte[] request) throws IOException {
        this.out.write(request);
        this.out.write(IOSocketService.EOL);
        this.out.flush();

        logger.info("Request: "+ new String(request));
    }

    @Override
    public byte[] receive() throws IOException {
        int dataSize = this.waitData();
        byte[] data = new byte[dataSize];
        int received = this.in.read(data, 0, dataSize);

        // Try to receive all data
        while (data[data.length - 1] != EOL) {
            dataSize = this.waitData();
            byte[] newData = new byte[dataSize];
            received += this.in.read(data, 0, dataSize);
            data = IOSocketService.concatArrays(data, newData);
        }

        logger.info("Response "+ received +" bytes: "+ new String(data));
        return data;
    }

    @Override
    public boolean isConnected() {
        return this.sc.isConnected();
    }

    private int waitData() throws IOException {
        int lag = 0;
        int dataSize = 0;
        while (dataSize < 1) {
            dataSize = this.in.available();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                logger.error(e);
            }

            if (lag > 100) {
                throw new IOException("Server is not responding");
            }

            lag++;
        }

        return dataSize;
    }

    protected static byte[] concatArrays(byte[] arr1, byte[] arr2) {
        byte[] newArray = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, newArray, 0, arr1.length);
        System.arraycopy(arr2, 0, newArray, arr1.length, arr2.length);

        return newArray;
    }

}
