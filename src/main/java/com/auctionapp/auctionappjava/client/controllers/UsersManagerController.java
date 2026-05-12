package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
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
// CHẶN(BAN): acc cấm đăng nhập

public class UsersManagerController implements Initializable {

    //để confirm thực hiện đúng mục dích
    private boolean ban =  false;

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
    private ComboBox<String> cbFilterAccountStatus;
    @FXML
    private ComboBox<String> cbFilterRoute;
    @FXML
    private TableView<UserDetailResponse> listUsers;
    @FXML
    private TableColumn<UserDetailResponse, Boolean> clmAccountStatus;
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
        ban = false;
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

        setupRowDoubleClick();
    }

    public void orConfirm(boolean choose) {
        // nút xác nhận-hủy-khung chọn chỉ khi bấm remove/ban/promoteAdmin
        btnConfirm.setVisible(choose);
        btnConfirm.setManaged(choose);
        clmChoose.setVisible(choose);
        btnCancel.setVisible(choose);
        btnCancel.setManaged(choose);
        btnBan.setManaged(!choose);
        btnBan.setVisible(!choose);
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

    }

    private void banUserFromServer(UserDetailResponse userDetailResponse) {

        ManagerAndHistoryRequest banReq = new ManagerAndHistoryRequest(userDetailResponse.userId());
        Request req = new Request("BAN_USER", banReq);
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
                    for (int i = 0; i < usersData.size(); i++) {
                        UserDetailResponse user = usersData.get(i);

                        if (user.userId().equals(userDetailResponse.userId())) {
                            // Tạo đối tượng mới với giá trị boolean isActive là false
                            // Giả sử UserDetailResponse là một Record (Java 14+)
                            UserDetailResponse updatedUser = new UserDetailResponse(
                                    user.userId(),
                                    user.latestBid(),
                                    user.fullName(),
                                    user.role(),
                                    user.balance(),
                                    false, // Đổi isActive thành false ở đây
                                    user.bids()
                            );

                            // Cập nhật lại vào ObservableList tại vị trí cũ
                            usersData.set(i, updatedUser);
                            break; // Tìm thấy rồi thì thoát vòng lặp
                        }
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
                    alert.show();
                }

                if (usersData.isEmpty()) {
                    Label noDataLabel = new Label("Hiện tại chưa có phiên đấu giá nào.");
                    noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                    listUsers.setPlaceholder(noDataLabel);
                }
            });
        });
    }

    private void setupRowDoubleClick() {
        listUsers.setRowFactory(tv -> {
            TableRow<UserDetailResponse> row = new TableRow<>();

            row.setOnMouseClicked(event -> {

                if (event.getClickCount() == 2 && !row.isEmpty()) {

                    if (ban) {

                        Runnable finalWarning = () -> {
                            banUserFromServer(row.getItem());
                        };

                        AlertUtils.ConfirmAlertController(
                                null,
                                "CẢNH BÁO!",
                                "NGƯỜI DÙNG NÀY SẼ BỊ CHẶN",
                                "BẠN CÓ MUỐN KHÔNG?",
                                "ĐÃ XONG",
                                "NGƯỜI DÙNG NÀY ĐÃ BỊ CHẶN",
                                "",
                                finalWarning,
                                null
                        );

                    } else {
                        System.out.println("Not ban");
                    }

                }
            });
            return row;
        });
    }

    private void setupColumns() {
        clmAccountStatus.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().accStatus()));
        clmName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().fullName()));
        clmRoute.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().role()));
        clmBids.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().bids()));
        clmBalance.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().balance()));
        clmRecentBid.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().latestBid()));

    }
}



