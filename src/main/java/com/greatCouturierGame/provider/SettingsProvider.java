package com.greatCouturierGame.provider;

import com.greatCouturierGame.data.AppSettings;

import java.io.IOException;

public interface SettingsProvider {
    AppSettings getSettings();
    void saveSettings(AppSettings appSettings) throws IOException;
}
