package controller;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/MainScreen.fxml")
            );
            Parent root = loader.load();
            primaryStage.setTitle("Ứng dụng JavaFX");
            Scene mainScene = new Scene(root, 1100, 600);
            primaryStage.setScene(mainScene);
            primaryStage.show();
            primaryStage.setResizable(false);
        } catch (IOException e) {
        e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
