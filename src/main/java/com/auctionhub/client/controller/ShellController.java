package com.auctionhub.client.controller;

import com.auctionhub.client.core.ClientContext;
import com.auctionhub.client.service.AlertService;
import com.auctionhub.client.service.SceneNavigator;
import com.auctionhub.client.session.ClientSession;
import com.auctionhub.client.util.RoleIconResolver;
import com.auctionhub.common.dto.AuthUserDto;
import com.auctionhub.common.enums.Role;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class ShellController {
    @FXML
    private Label currentUserLabel;
    @FXML
    private Label roleBadgeLabel;
    @FXML
    private Button sellerDashboardButton;
    @FXML
    private Button adminDashboardButton;
    @FXML
    private StackPane contentHost;
    @FXML
    private BorderPane chatbotHost;

    @FXML
    public void initialize() {
        AuthUserDto user = ClientSession.getInstance().getCurrentUser();
        if (user == null) {
            SceneNavigator.getInstance().showLogin();
            return;
        }
        currentUserLabel.setText(user.fullName() + " (" + user.username() + ")");
        roleBadgeLabel.setText(RoleIconResolver.iconFor(user.role()) + " " + user.role().name());
        sellerDashboardButton.setVisible(user.role() == Role.SELLER);
        adminDashboardButton.setVisible(user.role() == Role.ADMIN);
        loadChatbot();
        openAuctionDashboard();
    }

    @FXML
    private void openAuctionDashboard() {
        setContent("/client/fxml/AuctionDashboardView.fxml");
    }

    @FXML
    private void openSellerDashboard() {
        setContent("/client/fxml/SellerDashboardView.fxml");
    }

    @FXML
    private void openAdminDashboard() {
        setContent("/client/fxml/AdminDashboardView.fxml");
    }

    @FXML
    private void handleLogout() {
        try {
            ClientContext.getInstance().api().logout();
        } catch (Exception ex) {
            ClientContext.getInstance().socketClient().disconnect();
        } finally {
            ClientSession.getInstance().clear();
            SceneNavigator.getInstance().showLogin();
            AlertService.info("Đăng xuất", "Bạn đã đăng xuất an toàn khỏi hệ thống.");
        }
    }

    private void loadChatbot() {
        Parent chatbot = SceneNavigator.getInstance().load("/client/fxml/ChatbotPanel.fxml");
        chatbotHost.setCenter(chatbot);
    }

    private void setContent(String resource) {
        ClientContext.getInstance().socketClient().clearEventListeners();
        Parent root = SceneNavigator.getInstance().load(resource);
        contentHost.getChildren().setAll(root);
    }
}
