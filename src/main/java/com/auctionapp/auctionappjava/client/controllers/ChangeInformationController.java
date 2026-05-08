package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.ChangeInformationRequest;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

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

                ChangeInformationRequest payload = new ChangeInformationRequest(UserSession.getInstance().getCurrentUser().id(), txtFullname.getText() , txtEmail.getText());
                Request changeInformationRequest = new Request("CHANGE_INFORMATION", payload);
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return Client.getInstance().sendRequest(changeInformationRequest);
                    } catch (Exception e) {
                        return new Response(false, "Lỗi kết nối máy chủ!", null);
                    }
                });
            };



            AlertUtils.SceneOffAlertController(event,
                    "Chắc chưa?",
                    "Bạn có muốn đổi thông tin không?",
                    "",
                    "Thông báo",
                    "",
                    "Đã thay đổi thành công!",
                    pseudoMethod,
                    null);
        }
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
