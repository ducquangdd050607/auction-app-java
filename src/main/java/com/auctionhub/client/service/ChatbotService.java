package com.auctionhub.client.service;

import java.util.List;

public interface ChatbotService {
    String answer(String question);

    List<String> suggestedQuestions();
}
