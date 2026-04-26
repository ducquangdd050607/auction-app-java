package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// Giải thích:
// HOẠT ĐỘNG(ACTIVE): acc còn khỏe, còn chơi được
// CHẶN(BAN): acc vi phạm luật lệ bị cảnh cáo và cấm đăng nhập trong 1 thời gian
// XÓA(REMOVE): acc vi phạm nghiêm trọng/lặp lại nhiều lần sẽ bị tùng xẻo

public class UsersManagerController implements Initializable {

    //để confirm thực hiện đúng mục dích
    private boolean ban =  false;
    private boolean remove = false;

    @FXML
    private HBox box;
    @FXML
    private Button btnBan;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnPromoteAdmin;
    @FXML
    private Button btnRemove;
    @FXML
    private ComboBox<String> cbFilterAccountStatus;
    @FXML
    private ComboBox<String> cbFilterRoute;
    @FXML
    private TableColumn<?, ?> clmAccountStatus;
    @FXML
    private TableColumn<?, ?> clmBalance;
    @FXML
    private TableColumn<?, ?> clmBids;
    @FXML
    private TableColumn<?, ?> clmChoose;
    @FXML
    private TableColumn<?, ?> clmName;
    @FXML
    private TableColumn<?, ?> clmRecentBid;
    @FXML
    private TableColumn<?, ?> clmRoute;
    @FXML
    private TableView<?> listAuctions;
    @FXML
    private TextField txtSearch;


    @FXML
    void handleBan(ActionEvent event) {
        orConfirm(true);
        ban = true;
        remove = false;
    }

    @FXML
    void handleRemove(ActionEvent event) {
        orConfirm(true);
        remove = true;
        ban = false;
    }

    @FXML
    void handleConfirm(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chắc chưa?");
        alert.setHeaderText("Bạn có chắc chắn không?");

        alert.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {
                // if (admin == true)...
                alert.close();

                btnConfirm.setVisible(false);
                btnConfirm.setManaged(false);
                clmChoose.setVisible(false);
            } else {//xử lý hủy(chắc chỉ thế này)
                alert.close();

            }
        });
        orConfirm(false);
    }

    @FXML
    void handleCancel(ActionEvent event) {
        orConfirm(false);
    }

    @FXML
    void handleSearch(ActionEvent event) {

    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) { 
        String[] status = {"HOẠT DỘNG", "CHẶN", "XÓA"}; //trạng thái tài khoản
        cbFilterAccountStatus.getItems().addAll(status);

        String[] routes = {"BIDDER", "SELLER", "ADMIN"};// route
        cbFilterRoute.getItems().addAll(routes);

        try {
            show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void orConfirm(boolean choose) {
        // nút xác nhận-hủy-khung chọn chỉ khi bấm remove/ban/promoteAdmin
        btnConfirm.setVisible(choose);
        btnConfirm.setManaged(choose);
        clmChoose.setVisible(choose);
        btnCancel.setVisible(choose);
        btnCancel.setManaged(choose);
        btnBan.setManaged(!choose);
        btnRemove.setManaged(!choose);
        btnBan.setVisible(!choose);
        btnRemove.setVisible(!choose);
    }

    public void show() throws IOException {

        orConfirm(false);
    }
}



