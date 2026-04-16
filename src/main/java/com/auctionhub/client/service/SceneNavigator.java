package com.auctionhub.client.service;

import com.auctionhub.client.core.ClientContext;
import com.auctionhub.client.session.ClientSession;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneNavigator {
    private static final SceneNavigator INSTANCE = new SceneNavigator();
    private Stage primaryStage;

    private SceneNavigator() {
    }

    public static SceneNavigator getInstance() {
        return INSTANCE;
    }

    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setMinWidth(1200);
        this.primaryStage.setMinHeight(760);
    }

    public void showLogin() {
        ClientContext.getInstance().initialize();
        ClientContext.getInstance().socketClient().disconnect();
        ClientSession.getInstance().clear();
        show("/client/fxml/LoginView.fxml", "AuctionHub - Login");
    }

    public void showRegister() {
        show("/client/fxml/RegisterView.fxml", "AuctionHub - Register");
    }

    public void showShell() {
        if (!ClientSession.getInstance().isAuthenticated()) {
            showLogin();
            return;
        }
        show("/client/fxml/ShellView.fxml", "AuctionHub - Dashboard");
    }

    public Parent load(String resource) {
        try {
            return FXMLLoader.load(getClass().getResource(resource));
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể load FXML: " + resource, ex);
        }
    }

    private void show(String resource, String title) {
        Parent root = load(resource);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/client/css/app.css").toExternalForm());
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
