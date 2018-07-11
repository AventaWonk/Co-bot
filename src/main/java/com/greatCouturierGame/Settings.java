package com.greatCouturierGame;

public final class Settings {
    private String accountsFile;
    private String accountPartsSeparator;
    private boolean accountValidation;
    private boolean serverMode;
    private boolean debugMode;

    public static Settings getDefault() {
        return new Settings()
                .setAccountsFile("accounts.txt")
                .setAccountPartsSeparator(":")
                .setAccountValidation(true)
                .setDebugMode(true)
                .setServerMode(false);
    }

    public String getAccountsFile() {
        return accountsFile;
    }

    public Settings setAccountsFile(String accountsFile) {
        this.accountsFile = accountsFile;
        return this;
    }

    public String getAccountPartsSeparator() {
        return accountPartsSeparator;
    }

    public Settings setAccountPartsSeparator(String accountPartsSeparator) {
        this.accountPartsSeparator = accountPartsSeparator;
        return this;
    }

    public boolean isAccountValidation() {
        return accountValidation;
    }

    public Settings setAccountValidation(boolean accountValidation) {
        this.accountValidation = accountValidation;
        return this;
    }

    public boolean isServerMode() {
        return serverMode;
    }

    public Settings setServerMode(boolean serverMode) {
        this.serverMode = serverMode;
        return this;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public Settings setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }
}
