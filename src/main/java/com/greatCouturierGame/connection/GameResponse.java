package com.greatCouturierGame.connection;

import java.io.IOException;
import java.util.Map;

public class GameResponse {

    private Map<String, String> response;

    public GameResponse(Map<String, String> response) {
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
