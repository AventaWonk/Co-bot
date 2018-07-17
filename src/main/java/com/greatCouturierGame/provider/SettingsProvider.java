package com.greatCouturierGame.provider;

import com.greatCouturierGame.data.Settings;

import java.io.IOException;

public interface SettingsProvider {
    Settings getSettings();
    void saveSettings(Settings settings) throws IOException;
}
