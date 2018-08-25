package com.greatCouturierGame.connection;

import com.greatCouturierGame.adapter.IOSocketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameResponse {

    private static final Logger logger = LogManager.getLogger(GameResponse.class);

    private Map<String, String> responseMap;
    
    public GameResponse() {
        this.responseMap = new HashMap<>();
    }

    public GameResponse(byte[] responseData) {
        this.responseMap = GameResponse.parseResponse(responseData);
    }

    public void addPart(byte[] responseData) {
        this.responseMap.putAll(GameResponse.parseResponse(responseData));
    }


    public GameResponse shouldContain(String queryType) throws IOException {
        if (!this.responseMap.containsKey(queryType)) {
            throw new IOException("Response does not contain query "+ queryType);
        }

        return this;
    }

    public GameResponse shouldNotContain(String queryType) throws IOException {
        if (this.responseMap.containsKey(queryType)) {
            throw new IOException("Response contains query "+ queryType);
        }

        return this;
    }

    public QueryParserImpl getQuery(String queryType) {
        if (queryType.isEmpty() || !responseMap.containsKey(queryType)) {
            return null;
        }

        return new QueryParserImpl(responseMap.get(queryType));
    }

    public boolean isContains(String queryType) {
        return this.responseMap.containsKey(queryType);
    }

    public Map<String, String> getResponseMap() {
        return responseMap;
    }

    protected static Map<String, String> parseResponse(byte[] responseData) {
        logger.info("Response parsing started");
        Map<String, String> response = new HashMap<>();
        int lastDelimiter = -1;
        for (int i = 0; i < responseData.length; i++) {
            if (responseData[i] == IOSocketService.EOL) {
                String subResponse = new String(responseData, lastDelimiter + 1,  i - lastDelimiter);
                String type = QueryParser.getType(subResponse);
                if (type == null) {
                    logger.error("Type was not found\n"+
                            "Response: "+ subResponse);
                    continue;
                }

                response.put(type, subResponse.substring(type.length() + 6, subResponse.length() - 1));
                lastDelimiter = i;
                logger.info("Type "+ type +" parsed");
            }
        }

        logger.info("Parsing successfully completed");

        return response;
    }

}
