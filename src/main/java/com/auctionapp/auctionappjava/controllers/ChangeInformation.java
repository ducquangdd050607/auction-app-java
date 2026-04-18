package com.auctionapp.auctionappjava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class ChangeInformation {

    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    private Label lblError;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtUsername;

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {
        // TODO: Thay đổi thông tin trong database

        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        if (txtEmail.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty()) {
            lblError.setText("Hãy điền tất cả thông tin");
            lblError.setVisible(true);
            lblError.setTextFill(Color.web("#FF8A80"));
        } else {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Chắc chưa?");
            alert.setHeaderText("Bạn có muốn đổi thông tin không?");

            alert.showAndWait().ifPresent(response -> {

                if (response == ButtonType.OK) {

                    alert.close();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Thông báo");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Đã thay đổi thông tin thành công!");
                    successAlert.showAndWait();
                    currentStage.close();
                } else {
                    alert.close();
                }
            });
        }
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
