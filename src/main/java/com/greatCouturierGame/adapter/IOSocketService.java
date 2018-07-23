package com.greatCouturierGame.adapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class IOSocketService implements SocketService {

    private static final byte EOL = (byte) 0x00;

    private Socket sc;
    private BufferedInputStream in;
    private BufferedOutputStream out;

    public IOSocketService() {
        this.sc = new Socket();
        try {
            this.sc.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect(String ip, int port) throws IOException {
        this.sc.connect(new InetSocketAddress(ip, port));
        this.in = new BufferedInputStream(this.sc.getInputStream(), 1024);
        this.out = new BufferedOutputStream(this.sc.getOutputStream(), 1024);
    }

    @Override
    public void disconnect() throws IOException {
        this.sc.close();
    }

    @Override
    public void send(byte[] request) throws IOException {
        this.out.write(request);
        this.out.write(IOSocketService.EOL);
        this.out.flush();
    }

    @Override
    public byte[] receive() throws IOException {
        int dataSize = 0;
        while (dataSize < 1) {
            dataSize = this.in.available();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {

            }
        }

        byte[] data = new byte[dataSize];
        this.in.read(data, 0 , dataSize);

        return data;
    }

    @Override
    public boolean isConnected() {
        return this.sc.isConnected();
    }

}
