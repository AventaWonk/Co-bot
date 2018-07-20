package com.greatCouturierGame.connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameResponse {

    private Map<String, String> response;

    public GameResponse(byte[] responseData) {
        Map<String, String> response = new HashMap<>();
        int lastEOL = -1;
        for (int i = 0; i < responseData.length; i++) {
            if (responseData[i] == 0) {
                String subResponse = new String(responseData, lastEOL + 1,  i - lastEOL);
                String type = QueryParser.getType(subResponse);
                response.put(type, subResponse);
                lastEOL = i;
            }
        }

        this.response = response;
    }


    public GameResponse shouldContain(String queryType) throws IOException {
        if (!this.response.containsKey(queryType)) {
            throw new IOException("Response does not contain query "+ queryType);
        }

        return this;
    }

    public GameResponse shouldNotContain(String queryType) throws IOException {
        if (this.response.containsKey(queryType)) {
            throw new IOException("Response contains query "+ queryType);
        }

        return this;
    }

    public Map<String, String> getResponse() {
        return response;
    }

    public QueryParserImpl getQuery(String queryType) throws IOException  {
        if (queryType.isEmpty() || !response.containsKey(queryType)) {
            throw new IOException("Response does not contain query "+ queryType);
        }

        return new QueryParserImpl(response.get(queryType));
    }

    public boolean isContains(String queryType) {
        return this.response.containsKey(queryType);
    }
}
