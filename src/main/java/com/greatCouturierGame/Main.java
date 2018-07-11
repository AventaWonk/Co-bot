package com.greatCouturierGame;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Main {
    public static final Logger logger = LogManager.getLogger("FileLogger");
    protected static final String SETTINGS_FILE = "settings.json";

    public static void main(String[] args) {
        File settingsFile = new File(Main.SETTINGS_FILE);
        Settings settings;
        try {
            if (!settingsFile.exists()) {
                Main.createSettingsFile();
            }

            ObjectMapper mapper = new ObjectMapper();
            settings = mapper.readValue(settingsFile, Settings.class);
        } catch (IOException e) {
            Main.logger.error("Bad settings file");
            settings = Settings.getDefault();
        }

        final String accountsFileName = settings.getAccountsFile();
        Map<String, String> accountsList = new HashMap<>();
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(accountsFileName))) {
                Main.logger.info("Accounts file found successfully");
                int currentLine = 1;
                String accountString;
                while ((accountString = br.readLine()) != null) {
                    String[] accountParts = accountString.split(settings.getAccountPartsSeparator());
                    if (settings.isAccountValidation()) {
                        boolean userIsValid = Main.isUserIdValid(accountParts[0]);
                        boolean userAuthTokenIsValid = Main.isUserAuthTokenValid(accountParts[1]);
                        if (userIsValid || userAuthTokenIsValid) {
                            Main.logger.error("Error in accounts file"+ accountsFileName
                                    +" at line: "+ currentLine
                                    +", id is valid: "+ userIsValid
                                    +", auth token is valid: " + userAuthTokenIsValid);
                            continue;
                        }
                    }

                    accountsList.put(accountParts[0], accountParts[1]);
                    Main.logger.info("Account verified successfully at line: "+ currentLine);
                    currentLine++;
                }
            }

            if (accountsList.size() < 1) {
                Main.logger.error("Accounts list is empty");
                System.exit(1);
            }

            BiConsumer<String, String> engine = (uid, authToken) -> {
                Bot bot = new Bot(uid, authToken);
                Thread botThread = new Thread(bot);
                botThread.start();
            };

            accountsList.forEach(engine);
        } catch (FileNotFoundException e) {
            Main.logger.fatal("File "+ accountsFileName +" not found!");
            Main.createAccountsFile(accountsFileName);
        } catch (IOException e) {
            Main.logger.fatal(e);
        }
    }

    protected static boolean isUserIdValid(String id) {
        return id.matches("^\\d{1,17}$");
    }

    protected static boolean isUserAuthTokenValid(String token) {
        return token.matches("^\\w{32}$");
    }

    protected static void createAccountsFile(String fileName) {
        File file = new File(fileName);
        try {
            file.createNewFile();
            Main.logger.info("Accounts file "+ fileName +" created successfully");
        } catch (IOException e) {
            Main.logger.fatal("Can't create accounts file with name "+ fileName);
        }
    }

    protected static void createSettingsFile() throws IOException {
        new ObjectMapper()
                .writeValue(new File(Main.SETTINGS_FILE), Settings.getDefault());
//        Main.logger.fatal("Can't create settings file!");
        Main.logger.info("Settings file created successfully");
    }

}
