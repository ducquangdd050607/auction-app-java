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
    private boolean addAdmin = false;

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
        orConfirm();
        ban = true;
    }

    @FXML
    void handleRemove(ActionEvent event) {
        orConfirm();
        remove = true;
    }

    @FXML
    void handlePromoteAdmin(ActionEvent event) {
        orConfirm();
        addAdmin = true;
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

        orNotConfirm();
    }

    @FXML
    void handleCancel(ActionEvent event) {

        orNotConfirm();
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

    public void orConfirm() {
        // nút xác nhận-hủy-khung chọn chỉ khi bấm remove/ban/promoteAdmin
        btnConfirm.setVisible(true);
        btnConfirm.setManaged(true);
        clmChoose.setVisible(true);
        btnCancel.setVisible(true);
        btnCancel.setManaged(true);
        btnBan.setManaged(false);
        btnRemove.setManaged(false);
        btnPromoteAdmin.setManaged(false);
        btnBan.setVisible(false);
        btnRemove.setVisible(false);
        btnPromoteAdmin.setVisible(false);
    }

    public void orNotConfirm() {
        // nút remove-ban-promoteAdmin chỉ khi bấm cancel
        btnConfirm.setVisible(false);
        btnConfirm.setManaged(false);
        clmChoose.setVisible(false);
        btnCancel.setVisible(false);
        btnCancel.setManaged(false);
        btnBan.setManaged(true);
        btnRemove.setManaged(true);
        btnPromoteAdmin.setManaged(true);
        btnBan.setVisible(true);
        btnRemove.setVisible(true);
        btnPromoteAdmin.setVisible(true);
    }

    public void show() throws IOException {

        orNotConfirm();

        //sao không làm cái này từ trước:((
    }
}



