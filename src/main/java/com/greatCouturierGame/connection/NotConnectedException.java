package com.greatCouturierGame.connection;

public class NotConnectedException extends Exception {
    public NotConnectedException(String msg) {
        super(msg);

    }

    public NotConnectedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
