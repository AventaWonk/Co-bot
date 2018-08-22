package com.greatCouturierGame.logic;

import com.greatCouturierGame.connection.GameAPI;
import com.greatCouturierGame.connection.NotConnectedException;
import com.greatCouturierGame.connection.WrongCredentialsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

public class SimpleBotStrategy extends AbstractBotStrategy {

    private static final Logger logger = LogManager.getLogger(SimpleBotStrategy.class);

    private GameAPI gameAPI;

    public SimpleBotStrategy(GameAPI gameAPI) {
        super(gameAPI);
        this.gameAPI = gameAPI;
    }

    @Override
    public long doPlayerActions(String uid, String authToken) {
        try {
            this.gameAPI.syncTimeWithServer();
            this.gameAPI.connect(uid, authToken);
            long[] actionsNextTime = new long[3];

            try {
                final long nextTaskTime = this.doTaskAction();
                actionsNextTime[2] = nextTaskTime;
                Player.addDelay();

                final long nextPodiumTime = this.doPodiumAction();
                actionsNextTime[0] = nextPodiumTime;
                Player.addDelay();

                final long nextShopTime = this.doShopAction();
                actionsNextTime[1] = nextShopTime;
                Player.addDelay();

                this.gameAPI.disconnect();
                long soonActionTime = Arrays.stream(actionsNextTime).min().getAsLong();
            } catch (IOException e) {
                logger.error("Connection error");
                logger.error(e);
            }
        } catch (WrongCredentialsException | NotConnectedException e) {
            logger.fatal(e);
        } catch (IOException e) {
            logger.error(e);
        }

        return 0;
    }

}
