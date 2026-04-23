package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.service.ChatbotService;
import com.auctionapp.auctionappjava.client.service.RuleBasedChatbotService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatbotController {
    private final ChatbotService chatbotService = new RuleBasedChatbotService();
    @FXML private TextArea txtConversation;
    @FXML private TextField txtMessage;
    @FXML public void initialize(){ txtConversation.setText("Bot: Xin chào! Bạn có thể hỏi về login, tạo auction, đặt giá, auto bid, wallet hoặc dashboard.\n"); }
    @FXML void handleSend(ActionEvent event){ String msg=txtMessage.getText(); if(msg==null||msg.isBlank()) return; txtConversation.appendText("Bạn: " + msg + "\n"); txtConversation.appendText("Bot: " + chatbotService.answer(msg) + "\n"); txtMessage.clear(); }
    @FXML void handleClear(ActionEvent event){ initialize(); }
}
