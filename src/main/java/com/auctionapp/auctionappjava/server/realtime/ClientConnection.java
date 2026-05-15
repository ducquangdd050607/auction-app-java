package com.auctionapp.auctionappjava.server.realtime;

import com.auctionapp.auctionappjava.common.dto.AuctionRealtimeEvent;
import com.auctionapp.auctionappjava.common.dto.Response;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class ClientConnection {
    private final ObjectOutputStream out;
    private final Object writeLock = new Object();

    public ClientConnection(ObjectOutputStream out) {
        this.out = out;
    }

    public void sendResponse(Response response) throws IOException {
        sendObject(response);
    }

    public void sendEvent(AuctionRealtimeEvent event) throws IOException {
        sendObject(event);
    }

    private void sendObject(Object object) throws IOException {
        synchronized (writeLock) {
            out.writeObject(object);
            out.flush();
            out.reset();
        }
    }
}
