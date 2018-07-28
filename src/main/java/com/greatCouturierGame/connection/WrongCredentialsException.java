package com.greatCouturierGame.connection;

public class WrongCredentialsException extends Exception {

    public WrongCredentialsException(String msg) {
        super(msg);
    }

    public WrongCredentialsException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
