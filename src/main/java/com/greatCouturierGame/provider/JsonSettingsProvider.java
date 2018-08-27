package com.greatCouturierGame.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greatCouturierGame.data.AppSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class JsonSettingsProvider implements SettingsProvider {

    private static final Logger logger = LogManager.getLogger(JsonSettingsProvider.class);
    private String fileName;

    public JsonSettingsProvider(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public AppSettings getSettings() {
        File settingsFile = new File(this.fileName);
        AppSettings appSettings;
        try {
            if (!settingsFile.exists()) {
                JsonSettingsProvider.createDefaultSettingsFile(this.fileName);
                return AppSettings.getDefault();
            }

            logger.info("AppSettings file successfully found");
            ObjectMapper mapper = new ObjectMapper();
            appSettings = mapper.readValue(settingsFile, AppSettings.class);
        } catch (IOException e) {
            logger.fatal("Bad appSettings file");
            appSettings = AppSettings.getDefault();
        }

        return appSettings;
    }

    @Override
    public void saveSettings(AppSettings appSettings) throws IOException {
        File settingsFile = new File(this.fileName);
        new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValue(settingsFile, appSettings);

        logger.info("AppSettings file successfully saved");
    }

    protected static void createDefaultSettingsFile(String fileName) throws IOException {
        File settingsFile = new File(fileName);
        new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValue(settingsFile, AppSettings.getDefault());

        logger.info("AppSettings file successfully created");
    }
}
