package com.auctionhub.client.controller;

import com.auctionhub.client.core.ClientContext;
import com.auctionhub.client.service.ChatbotService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatbotController {
    @FXML
    private TextArea conversationArea;
    @FXML
    private TextField questionField;
    @FXML
    private ListView<String> suggestionListView;

    private ChatbotService chatbotService;

    @FXML
    public void initialize() {
        chatbotService = ClientContext.getInstance().chatbotService();
        suggestionListView.setItems(FXCollections.observableArrayList(chatbotService.suggestedQuestions()));
        suggestionListView.setOnMouseClicked(event -> {
            String selected = suggestionListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                questionField.setText(selected);
                send();
            }
        });
        append("Bot", "Xin chào, tôi có thể hướng dẫn đăng ký, đăng nhập, đặt bid, auto-bid, anti-sniping và trạng thái phiên.");
    }

    @FXML
    private void send() {
        String question = questionField.getText();
        if (question == null || question.isBlank()) {
            return;
        }
        append("Bạn", question);
        append("Bot", chatbotService.answer(question));
        questionField.clear();
    }

    @FXML
    private void clearConversation() {
        conversationArea.clear();
        append("Bot", "Cuộc hội thoại đã được xóa. Bạn có thể hỏi tiếp bất cứ lúc nào.");
    }

    private void append(String speaker, String message) {
        conversationArea.appendText(speaker + ": " + message + System.lineSeparator() + System.lineSeparator());
    }
}
