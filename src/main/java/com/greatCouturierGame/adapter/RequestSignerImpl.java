package com.greatCouturierGame.adapter;

import com.greatCouturierGame.connection.QueryParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RequestSignerImpl implements RequestSigner {

    private static final Logger logger = LogManager.getLogger(RequestSignerImpl.class);
    private static final String serverKey = "33333";

    private String userKey;

    public RequestSignerImpl() {
        this.userKey = "";
    }

    @Override
    public String getSign(String request) {
        StringBuilder signSB = new StringBuilder();
        String requestType = QueryParser.getType(request);
        String hash = "";
        if (requestType != null && !requestType.equals("SyncTime")  && !requestType.equals("Connect")) {
            hash = getHashOfString(request + getSalt());
        }

        signSB.insert(0, "Sig:" + hash + ";");
        String requestSize = String.valueOf(signSB.length() + request.length());
        if (requestSize.length() == 1) {
            requestSize = "00" + requestSize;
        } else if (requestSize.length() == 2) {
            requestSize = "0" + requestSize;
        }

        signSB.insert(0, "Size:" + requestSize + ";");
        return signSB.toString();
    }

    @Override
    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    protected static String getHashOfString(String string) {
        MessageDigest md5MessageDigest = null;
        try {
            md5MessageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.fatal(e);
            System.exit(1);
        }

        md5MessageDigest.update(string.getBytes());
        String md5 = new BigInteger(1, md5MessageDigest.digest()).toString(16);
        if (md5.length() == 32) {
            return md5;
        }

        StringBuilder md5SB = new StringBuilder(md5);
        while (md5SB.length() < 32) {
            md5SB.insert(0, 0);
        }

        return md5SB.toString();
    }

    protected String getSalt() {
        return RequestSignerImpl.getHashOfString(this.userKey + RequestSignerImpl.serverKey);
    }

}
