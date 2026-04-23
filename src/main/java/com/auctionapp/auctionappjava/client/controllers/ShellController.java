package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.service.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ShellController {
    @FXML private Label lblStatus;
    private final SceneNavigator navigator = new SceneNavigator();
    @FXML public void initialize(){ if(lblStatus!=null) lblStatus.setText("Auction client shell sẵn sàng"); }
    @FXML void openChatbot(ActionEvent event){ try { navigator.showWindow("/com/auctionapp/auctionappjava/views/ChatbotScreen.fxml", "Trợ lý đấu giá"); } catch (Exception e) { if(lblStatus!=null) lblStatus.setText(e.getMessage()); } }
}
