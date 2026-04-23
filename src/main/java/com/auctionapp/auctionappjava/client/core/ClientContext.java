package com.auctionapp.auctionappjava.client.core;

import com.auctionapp.auctionappjava.client.config.ClientConfig;
import com.auctionapp.auctionappjava.client.network.*;
import com.auctionapp.auctionappjava.client.session.ClientSession;

public final class ClientContext {
    private static final ClientContext INSTANCE = new ClientContext();
    private final ClientSession session = new ClientSession();
    private final SocketClient socketClient = new SocketClient(ClientConfig.load());
    private final ClientApi api = new ClientApi(socketClient, session);
    private ClientContext() {}
    public static ClientContext getInstance(){ return INSTANCE; }
    public ClientSession getSession(){ return session; }
    public SocketClient getSocketClient(){ return socketClient; }
    public ClientApi getApi(){ return api; }
}
