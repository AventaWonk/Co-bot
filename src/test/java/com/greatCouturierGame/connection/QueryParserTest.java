package com.greatCouturierGame.connection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueryParserTest {

    private static List<String> testResponseList;

    static {
        testResponseList = Arrays.asList(
                "Type:SyncTimeResponse;ServerTime:1520639998765;",
                "Type:ConnectResponse;Name:Test;EyeColor:1;HairColor:2;SwimColor:3;NoseType:4;"
        );
    }

    @Test
    void getParameter() {
        QueryParser qp = new QueryParser(testResponseList.get(0));
        assertDoesNotThrow(() -> {
            assertEquals("1520639998765", qp.getParameter("ServerTime"));
        });

        assertThrows(NotContainsParameterException.class, () -> qp.getParameter("Test"));
    }

    @Test
    void getTypeOfQuery() {

    }

    @Test
    void getParameters() {
        QueryParser qp = new QueryParser(testResponseList.get(0));
        Map<String, String> parameters = qp.getParameters(
                "ServerTime",
                "Test"
        );

        assertEquals("1520631234567", parameters.get("ServerTime"));
        assertEquals(null, parameters.get("Server"));

        parameters = qp.setQuery(testResponseList.get(1)).getParameters(
                "Name",
                "SwimColor"
        );

        assertEquals("Test", parameters.get("Name"));
        assertEquals("3", parameters.get("SwimColor"));
    }
}