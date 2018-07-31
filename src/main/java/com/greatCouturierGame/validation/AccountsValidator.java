package com.greatCouturierGame.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AccountsValidator {

    private static final Logger logger = LogManager.getLogger(AccountsValidator.class);

    public static Map<String, String> getValid(Map<String, String> accountsMap) {
        Map<String, String> validAccounts = new HashMap<>();

        accountsMap.forEach((id, token) -> {
            boolean userIsValid = AccountsValidator.isUserIdValid(id);
            boolean userAuthTokenIsValid = AccountsValidator.isUserAuthTokenValid(token);
            if (userIsValid && userAuthTokenIsValid) {
                validAccounts.put(id, token);
            } else {
                logger.error("Invalid account " + id
                        + ", id is valid: " + userIsValid
                        + ", auth token is valid: " + userAuthTokenIsValid
                );
            }
        });


        return validAccounts;
    }

    protected static boolean isUserIdValid(String id) {
        return id.matches("^\\d{1,17}$");
    }

    protected static boolean isUserAuthTokenValid(String token) {
        return token.matches("^\\w{32}$");
    }

}
