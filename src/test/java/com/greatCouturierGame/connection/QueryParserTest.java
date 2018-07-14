package com.greatCouturierGame.connection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QueryParserTest {

    private static List<String> testResponseList = Arrays.asList(
                "Type:SyncTimeResponse;ServerTime:1520639998765;",
                "Type:ConnectResponse;Name:Test;EyeColor:1;HairColor:2;SwimColor:3;NoseType:;"
    );


    @Test
    void getParameter() {
        QueryParser qp = new QueryParser(testResponseList.get(0));

        assertEquals("1520639998765", qp.getParameter("ServerTime"));
        assertEquals("Test", qp.getParameter("Name"));
        assertEquals("", qp.getParameter("NoseType"));
        assertNull(qp.getParameter("Test"));
    }

    @Test
    void getTypeOfQuery() {
        String testTypeOfQuery1 = QueryParser.getTypeOfQuery(testResponseList.get(0));
        String testTypeOfQuery2 = QueryParser.getTypeOfQuery(testResponseList.get(2));

        assertEquals("SyncTimeResponse", testTypeOfQuery1);
        assertEquals("ConnectResponse", testTypeOfQuery2);
    }

    @Test
    void getParameters() {
        QueryParser qp = new QueryParser(testResponseList.get(0));
        Map<String, String> parameters = qp.getParameters(
                "ServerTime",
                "Test"
        );

        assertEquals("1520631234567", parameters.get("ServerTime"));
        assertNull(parameters.get("Test"));
        assertNull(parameters.get("Test2"));

        parameters = qp.setQuery(testResponseList.get(1))
                .getParameters(
                        "Name",
                        "SwimColor",
                        "NoseType"
                );

        assertEquals("Test", parameters.get("Name"));
        assertEquals("3", parameters.get("SwimColor"));
        assertEquals(3, parameters.size());
    }
}