package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.util.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class ChangeInformationController {

    @FXML
    private Label lblError;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtFullname;

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {
        // TODO: Thay đổi thông tin trong database

        if (txtEmail.getText().trim().isEmpty() || txtFullname.getText().trim().isEmpty()) {
            lblError.setText("Hãy điền tất cả thông tin");
            lblError.setVisible(true);
            lblError.setTextFill(Color.web("#FF8A80"));
        } else {

            //TODO: Logic cập nhật lại email, tên

            Runnable pseudoMethod = () -> { //Test
                System.out.println("PseudoMethod");
            };


            AlertUtils.SceneOffAlertController(event,
                    "Chắc chưa?",
                    "Bạn có muốn đổi thông tin không?",
                    "",
                    "Thông báo",
                    "",
                    "Đã thay đổi thành công!",
                    pseudoMethod);
        }
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
