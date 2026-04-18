package com.auctionapp.auctionappjava;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class JavaFXLaunch extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(JavaFXLaunch.class.getResource("views/MainScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 950, 650);
        stage.setTitle("Bid88");
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);

        // thông báo tắt bằng "X"
        stage.setOnCloseRequest(event -> {
            // Ngăn chặn việc đóng cửa sổ ngay lập tức để chờ xác nhận
            event.consume();
            handleExit(stage);
        });
    }

    private void handleExit(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thoát");
        alert.setHeaderText("Bạn có chắc chắn muốn thoát ứng dụng không?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                stage.close();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}