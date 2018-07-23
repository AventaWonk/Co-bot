package com.greatCouturierGame.adapter;

public interface RequestSigner {
    String getSign(String request);

    void setUserKey(String key);
}
