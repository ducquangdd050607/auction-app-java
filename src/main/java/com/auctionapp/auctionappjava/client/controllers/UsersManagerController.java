package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.dto.UserDetailResponse;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

// Giải thích:
// HOẠT ĐỘNG(ACTIVE): acc còn khỏe, còn chơi được
// CHẶN(BAN): acc vi phạm luật lệ bị cảnh cáo và cấm đăng nhập trong 1 thời gian
// XÓA(REMOVE): acc vi phạm nghiêm trọng/lặp lại nhiều lần sẽ bị tùng xẻo

public class UsersManagerController implements Initializable {

    //để confirm thực hiện đúng mục dích
    private boolean ban =  false;
    private boolean remove = false;

    private ObservableList<UserDetailResponse> usersData = FXCollections.observableArrayList();

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
    private TableView<UserDetailResponse> listUsers;
    @FXML
    private TableColumn<UserDetailResponse, String> clmAccountStatus;
    @FXML
    private TableColumn<UserDetailResponse, BigDecimal> clmBalance;
    @FXML
    private TableColumn<UserDetailResponse, Integer> clmBids;
    @FXML
    private TableColumn<UserDetailResponse, ?> clmChoose;
    @FXML
    private TableColumn<UserDetailResponse, String> clmName;
    @FXML
    private TableColumn<UserDetailResponse, String> clmRecentBid;
    @FXML
    private TableColumn<UserDetailResponse, String> clmRoute;
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

        String[] routes = {"BIDDER", "ADMIN", "SELLER"};// route
        cbFilterRoute.getItems().addAll(routes);

        setupColumns();
        listUsers.setItems(usersData);

        loadUserFromServer();

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

    private void loadUserFromServer() {
        Request req = new Request("GET_USERS", null);

        CompletableFuture.supplyAsync(() -> {
            try {
                return Client.getInstance().sendRequest(req);
            } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Lỗi kết nối Server", null);
            }
        }).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.success()) {
                    List<UserDetailResponse> listFromServer = (List<UserDetailResponse>) response.data();

                    usersData.setAll(listFromServer);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
                    alert.show();
                }
            });
        });
//
//    }
//
//    private void deleteUserFromServer() {
//        Request req = new Request("DELETE_USER", null);
//        CompletableFuture.supplyAsync(() -> {
//            try {
//                return Client.getInstance().sendRequest(req);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                return new Response(false, "Lỗi kết nối Server", null);
//            }
//        }).thenAccept(response -> {
//            Platform.runLater(() -> {
//                if (response.success()) {
//                    List<UserDetailResponse> listFromServer = (List<UserDetailResponse>) response.data();
//                    usersData.setAll(listFromServer);
//                } else {
//                    Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
//                    alert.show();
//                }
//            });
//        });
    }

    private void setupColumns() {
        clmAccountStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().accStatus()));
        clmName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().fullName()));
        clmRoute.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().role()));
        clmBids.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().bids()));
        clmBalance.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().balance()));

    }
}



