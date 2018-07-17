package com.greatCouturierGame.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greatCouturierGame.Main;
import com.greatCouturierGame.data.Settings;

import java.io.File;
import java.io.IOException;

public class JsonSettingsProvider implements SettingsProvider {

    private String fileName;

    public JsonSettingsProvider(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Settings getSettings() {
        File settingsFile = new File(this.fileName);
        Settings settings;
        try {
            if (!settingsFile.exists()) {
                JsonSettingsProvider.createDefaultSettingsFile(this.fileName);
                return Settings.getDefault();
            }

            Main.logger.info("Settings file found successfully");
            ObjectMapper mapper = new ObjectMapper();
            settings = mapper.readValue(settingsFile, Settings.class);
        } catch (IOException e) {
            Main.logger.fatal("Bad settings file");
            settings = Settings.getDefault();
        }

        return settings;
    }

    @Override
    public void saveSettings(Settings settings) throws IOException {
        File settingsFile = new File(this.fileName);
        new ObjectMapper().writeValue(settingsFile, settings);

        Main.logger.info("Settings file saved successfully");
    }

    protected static void createDefaultSettingsFile(String fileName) throws IOException {
        File settingsFile = new File(fileName);
        new ObjectMapper().writeValue(settingsFile, Settings.getDefault());

        Main.logger.info("Settings file created successfully");
    }
}
