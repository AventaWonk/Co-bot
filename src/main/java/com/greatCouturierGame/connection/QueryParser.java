package com.greatCouturierGame.connection;

import java.util.Map;

public interface QueryParser {

    String getParameter(String parameter);

    Map<String, String> getParameters(String... parameters);

    QueryParserImpl setQuery(String query);

    static String getType(String query) {
        int semicolonIndex = query.indexOf(";");
        if (semicolonIndex == -1) {
            semicolonIndex = query.length();
        }

        String queryType = query.substring(query.indexOf(":") + 1, semicolonIndex);
        if (queryType.isEmpty()) {
            return null;
        }

        return queryType;
    }

}
