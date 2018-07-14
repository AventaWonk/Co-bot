package com.greatCouturierGame.connection;

import java.util.HashMap;
import java.util.Map;

public class QueryParser {

    private String query;

    QueryParser(String query) {
        this.query = query;
    }

    public String getParameter(String parameter) {
        String fullParameter = parameter + ":";
        int parameterIndex = this.query.indexOf(fullParameter);
        int semicolonIndex = this.query.indexOf(";", parameterIndex);
        if (parameterIndex == -1 || parameterIndex < semicolonIndex) {
            return null;
        }

        if (semicolonIndex == -1 ) {
            return this.query.substring(parameterIndex + fullParameter.length());
        }

        return this.query.substring(parameterIndex + fullParameter.length(), semicolonIndex);
    }

    public Map<String, String> getParameters(String... parameters)  {
        Map<String, String> queryDataMap = new HashMap<>(parameters.length);
        for (String parameter : parameters) {
            queryDataMap.put(parameter, getParameter(parameter));
        }

        return queryDataMap;
    }

    protected static String getTypeOfQuery(String query) {
        int semicolonIndex = query.indexOf(";");
        if (semicolonIndex == -1) {
            semicolonIndex = query.length();
        }

        String queryType = query.substring(query.indexOf(":") + 1, semicolonIndex);
        if (queryType.length() < 1) {
            return null;
        }

        return queryType;
    }

    public QueryParser setQuery(String query) {
        this.query = query;
        return this;
    }

}
