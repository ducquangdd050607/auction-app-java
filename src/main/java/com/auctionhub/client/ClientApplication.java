package com.auctionhub.client;

import com.auctionhub.client.core.ClientContext;
import com.auctionhub.client.service.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        ClientContext.getInstance().initialize();
        SceneNavigator.getInstance().initialize(primaryStage);
        SceneNavigator.getInstance().showLogin();
    }

    @Override
    public void stop() {
        ClientContext.getInstance().socketClient().disconnect();
    }
}
