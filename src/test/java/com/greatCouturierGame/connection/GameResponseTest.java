package com.greatCouturierGame.connection;

import com.greatCouturierGame.adapter.IOSocketService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameResponseTest {

    private static final String testResponse1 = "Type:Test;Param:Test"+ (char) IOSocketService.EOL;
    private static final String testResponse2 = "Type:Test1;Param:Test1"+ (char) IOSocketService.EOL;
    private static final Map<String, String> expectedResponse1Map = new HashMap<>();
    private static final Map<String, String> expectedResponse2Map = new HashMap<>();

    static {
        expectedResponse1Map.put("Test", "Param:Test");
        expectedResponse2Map.put("Test1", "Param:Test1");
    }

    @Test
    void parseResponse() {
        Map<String, String> actualResponseMap = GameResponse.parseResponse(testResponse1.getBytes());

        assertEquals(expectedResponse1Map, actualResponseMap);
    }

    @Test
    void addPartTest() {
        GameResponse gameResponse = new GameResponse(testResponse1.getBytes());
        gameResponse.addPart(testResponse2.getBytes());
        Map<String, String> actualResponseMap = gameResponse.getResponseMap();

        Map<String, String> expectedResponseMap = new HashMap<>();
        expectedResponseMap.putAll(expectedResponse1Map);
        expectedResponseMap.putAll(expectedResponse2Map);

        assertEquals(expectedResponseMap, actualResponseMap);
    }

}