package com.auctionhub.client.core;

import com.auctionhub.client.config.ClientConfig;
import com.auctionhub.client.network.ClientApi;
import com.auctionhub.client.network.SocketClient;
import com.auctionhub.client.service.ChatbotService;
import com.auctionhub.client.service.RuleBasedChatbotService;

public final class ClientContext {
    private static final ClientContext INSTANCE = new ClientContext();

    private ClientConfig clientConfig;
    private SocketClient socketClient;
    private ClientApi clientApi;
    private ChatbotService chatbotService;

    private ClientContext() {
    }

    public static ClientContext getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        if (clientConfig != null) {
            return;
        }
        clientConfig = new ClientConfig();
        socketClient = new SocketClient(clientConfig.getHost(), clientConfig.getPort());
        clientApi = new ClientApi(socketClient);
        chatbotService = new RuleBasedChatbotService();
    }

    public SocketClient socketClient() {
        return socketClient;
    }

    public ClientApi api() {
        return clientApi;
    }

    public ChatbotService chatbotService() {
        return chatbotService;
    }
}
