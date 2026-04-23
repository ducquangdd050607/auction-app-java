package com.auctionapp.auctionappjava.client.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RuleBasedChatbotServiceTest {

    private final ChatbotService chatbot = new RuleBasedChatbotService();

    @Test
    void answersWalletIntent() {
        String answer = chatbot.answer("Tôi muốn nạp tiền vào wallet");
        assertTrue(answer.toLowerCase().contains("ví"));
        assertTrue(answer.toLowerCase().contains("nạp"));
    }

    @Test
    void returnsFallbackWhenIntentUnknown() {
        String answer = chatbot.answer("câu hỏi rất lạ không liên quan");
        assertTrue(answer.toLowerCase().contains("chưa hiểu"));
    }
}
