package com.greatCouturierGame.data;

public final class AppSettings {

    private String accountsFile;
    private String accountPartsSeparator;
    private boolean consoleLogger;
    private boolean accountValidation;
    private boolean serverMode;
    private boolean debugMode;

    public static AppSettings getDefault() {
        return new AppSettings()
                .setAccountsFile("accounts.txt")
                .setAccountPartsSeparator(":")
                .setConsoleLogger(true)
                .setAccountValidation(true)
                .setDebugMode(true)
                .setServerMode(false);
    }

    public String getAccountsFile() {
        return accountsFile;
    }

    public AppSettings setAccountsFile(String accountsFile) {
        this.accountsFile = accountsFile;
        return this;
    }

    public String getAccountPartsSeparator() {
        return accountPartsSeparator;
    }

    public AppSettings setAccountPartsSeparator(String accountPartsSeparator) {
        this.accountPartsSeparator = accountPartsSeparator;
        return this;
    }

    public boolean isConsoleLogger() {
        return consoleLogger;
    }

    public AppSettings setConsoleLogger(boolean consoleLogger) {
        this.consoleLogger = consoleLogger;
        return this;
    }

    public boolean isAccountValidation() {
        return accountValidation;
    }

    public AppSettings setAccountValidation(boolean accountValidation) {
        this.accountValidation = accountValidation;
        return this;
    }

    public boolean isServerMode() {
        return serverMode;
    }

    public AppSettings setServerMode(boolean serverMode) {
        this.serverMode = serverMode;
        return this;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public AppSettings setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }
}
