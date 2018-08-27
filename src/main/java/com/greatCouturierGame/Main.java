package com.greatCouturierGame;

import com.greatCouturierGame.adapter.*;
import com.greatCouturierGame.connection.GameAPI;
import com.greatCouturierGame.connection.SocketGameAPI;
import com.greatCouturierGame.data.AppSettings;
import com.greatCouturierGame.logic.AbstractBotStrategy;
import com.greatCouturierGame.logic.Bot;
import com.greatCouturierGame.logic.SimpleBotStrategy;
import com.greatCouturierGame.provider.AccountsProvider;
import com.greatCouturierGame.provider.FileAccountsProvider;
import com.greatCouturierGame.provider.JsonSettingsProvider;
import com.greatCouturierGame.provider.SettingsProvider;
import com.greatCouturierGame.validation.AccountsValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.BiConsumer;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        SettingsProvider settingsProvider = new JsonSettingsProvider("settings.json");
        AppSettings settings = settingsProvider.getSettings();

        AccountsProvider accountsProvider = new FileAccountsProvider(
                settings.getAccountsFile(),
                settings.getAccountPartsSeparator()
        );
        Map<String, String> accountsMap = accountsProvider.getAccounts();
        if (settings.isAccountValidation()) {
            accountsMap = AccountsValidator.getValid(accountsMap);
        }

        if (accountsMap.size() < 1) {
            Main.logger.error("Accounts list is empty");
            System.exit(1);
        }

        BiConsumer<String, String> engine = (uid, authToken) -> {
            RequestSigner requestSigner = new RequestSignerImpl();
            SocketService socketService = new IOSocketService();
            Communicator communicator = new SocketCommunicator(socketService, requestSigner);
            GameAPI gameAPI = new SocketGameAPI(communicator);

            AbstractBotStrategy botStrategy = new SimpleBotStrategy(gameAPI);
            Bot bot = new Bot(botStrategy);
            bot.run(uid, authToken);
        };
        accountsMap.forEach(engine);
    }

}
