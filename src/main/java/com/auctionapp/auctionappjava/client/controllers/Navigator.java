package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Navigator implements Initializable {

    private Stage stage;
    private Parent root;
    private Scene scene;
    protected static String modeName;

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Button changePassword;
    @FXML
    private VBox groupAccount;
    @FXML
    private VBox groupAdmin;
    @FXML
    private VBox groupBidder;
    @FXML
    private VBox groupHome;
    @FXML
    private VBox groupSeller;
    @FXML
    private Button setting;
    @FXML
    private Button wallet;
    @FXML
    private Button identity;
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnExit;

    // Thêm thuộc tính và hành vi điều khiển UI/UX navbar
    @FXML
    private Button currentActiveButton;
    private void setActiveButton(Button clickedButton) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-menu-btn-active");
        }
        clickedButton.getStyleClass().add("nav-menu-btn-active");
        currentActiveButton = clickedButton;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void show() throws IOException {
        // 1. Ẩn tất cả đi trước
        groupAdmin.setVisible(false);
        groupAdmin.setManaged(false);
        groupSeller.setVisible(false);
        groupSeller.setManaged(false);
        groupBidder.setVisible(false);
        groupBidder.setManaged(false);

        // 2. Lấy giá trị boolean từ class Route và kiểm tra
        if (Route.adminRoute) {
            groupAdmin.setVisible(true);
            groupAdmin.setManaged(true);
            identity.setText("ADMIN");

        } else if (Route.sellerRoute) {
            groupSeller.setVisible(true);
            groupSeller.setManaged(true);
            identity.setText("SELLER");

        } else if (Route.bidderRoute) {
            groupBidder.setVisible(true);
            groupBidder.setManaged(true);
            identity.setText("BIDDER");
        }
        setActiveButton(btnDashboard);
        currentActiveButton = btnDashboard;
        Parent scene1 = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/Dashboard.fxml"));
        mainBorderPane.setCenter(scene1);
    }

    @FXML
    void handleAccount(ActionEvent event) throws IOException {
        Button btnClicked = (Button) event.getSource();
        setActiveButton(btnClicked);
        Parent scene1 = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/AccountScreen.fxml"));
        mainBorderPane.setCenter(scene1);
    }

    @FXML
    void handleItemsList(ActionEvent event) throws IOException {
        Button btnClicked = (Button) event.getSource();
        setActiveButton(btnClicked);
        modeName = btnClicked.getText();
        Parent scene1 = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/AuctionListScreen.fxml"));
        mainBorderPane.setCenter(scene1);
    }

    @FXML
    void handleBackToDash(ActionEvent event) throws IOException {
        Button btnClicked = (Button) event.getSource();
        setActiveButton(btnClicked);
        Parent scene1 = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/Dashboard.fxml"));
        mainBorderPane.setCenter(scene1);
    }

    @FXML
    void handleGotoUsersManager(ActionEvent event) throws IOException {
        Button btnClicked = (Button) event.getSource();
        setActiveButton(btnClicked);
        Parent scene1 = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/UsersManager.fxml"));
        mainBorderPane.setCenter(scene1);
    }

    @FXML
    void handleSignOut(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chắc chưa?");
        alert.setHeaderText("Bạn có chắc muốn đăng xuất không?");

        alert.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                alert.close();
                try {
                    SceneSwitcherController.NewSceneController(event, "/com/auctionapp/auctionappjava/views/MainScreen.fxml", "Bíd88");

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } else {
                alert.close();

            }
        });
    }

    @FXML
    // Đổi Route
    void handleSwitcher(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chắc chưa?");
        alert.setHeaderText("Bạn có chắc muốn đổi vai trò không?");

        alert.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                alert.close();
                try {
                    SceneSwitcherController.NewSceneController(event, "/com/auctionapp/auctionappjava/views/Route.fxml", "Vai trò");

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } else {
                alert.close();

            }
        });
    }

    @FXML
    void handleExit (ActionEvent event) throws IOException {

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thoát");
        alert.setHeaderText("Bạn có chắc chắn muốn thoát ứng dụng không?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                stage.close();
            }
        });
    }
}

