package com.greatCouturierGame.provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileAccountsProvider implements AccountsProvider {

    private static final Logger logger = LogManager.getLogger(FileAccountsProvider.class);

    private String fileName;
    private String accountPartsSeparator;

    public FileAccountsProvider(String fileName, String accountPartsSeparator) {
        this.fileName = fileName;
        this.accountPartsSeparator = accountPartsSeparator;
    }

    @Override
    public Map<String, String> getAccounts() {
        Map<String, String> accountsMap = new HashMap<>();
        File accountsFile = new File(fileName);
        if (!accountsFile.exists()) {
            logger.error("File "+ fileName +" not found!");
            FileAccountsProvider.createAccountsFile(fileName);
            return accountsMap;
        }

        logger.info("Accounts file successfully found");
        try (BufferedReader br = new BufferedReader(new FileReader(accountsFile))) {
            String accountString;
            while ((accountString = br.readLine()) != null) {
                String[] accountParts = accountString.split(accountPartsSeparator);
                accountsMap.put(accountParts[0], accountParts[1]);
            }
        } catch (IOException e) {
            logger.fatal(e);
            return accountsMap;
        }

        return accountsMap;
    }

    protected static void createAccountsFile(String fileName)  {
        File file = new File(fileName);
        try {
            if (file.createNewFile()) {
                logger.info("Accounts file "+ fileName +" created successfully");
            }
        } catch (IOException e) {
            logger.fatal("Can't create accounts file with name "+ fileName);
        }
    }

}
