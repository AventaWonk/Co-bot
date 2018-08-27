package com.greatCouturierGame.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class QueryParserImpl implements  QueryParser {

    private static final Logger logger = LogManager.getLogger(QueryParserImpl.class);

    private String query;

    QueryParserImpl(String query) {
        this.query = query;
    }

    @Override
    public String getParameter(String parameter) {
        String fullParameter = parameter + ":";
        int parameterIndex = this.query.indexOf(fullParameter);
        int semicolonIndex = this.query.indexOf(";", parameterIndex);
        if (parameterIndex == -1) {
            logger.error("Parameter not found\n " +
                    "Parameter: "+ parameter + "\n " +
                    "Query: "+ this.query);

            return null;
        }

        if (semicolonIndex == -1 ) {
            return this.query.substring(parameterIndex + fullParameter.length());
        }

        return this.query.substring(parameterIndex + fullParameter.length(), semicolonIndex);
    }

    @Override
    public Map<String, String> getParameters(String... parameters)  {
        Map<String, String> queryDataMap = new HashMap<>(parameters.length);
        for (String parameter : parameters) {
            queryDataMap.put(parameter, getParameter(parameter));
        }

        return queryDataMap;
    }

    @Override
    public QueryParserImpl setQuery(String query) {
        this.query = query;
        return this;
    }

}
