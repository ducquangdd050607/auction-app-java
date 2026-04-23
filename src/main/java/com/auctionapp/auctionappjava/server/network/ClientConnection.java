package com.auctionapp.auctionappjava.server.network;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.RequestAction;
import com.auctionapp.auctionappjava.common.observer.AuctionEventListener;
import com.auctionapp.auctionappjava.common.util.JacksonSupport;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientConnection implements Runnable, AuctionEventListener, Closeable {
    private final Socket socket;
    private final RequestDispatcher dispatcher;
    private final SocketAuctionEventPublisher publisher;
    private final ClientSession session = new ClientSession();
    private BufferedReader in;
    private BufferedWriter out;
    private volatile boolean running = true;

    public ClientConnection(Socket socket, RequestDispatcher dispatcher, SocketAuctionEventPublisher publisher) {
        this.socket = socket;
        this.dispatcher = dispatcher;
        this.publisher = publisher;
    }

    @Override
    public void run() {
        try (socket) {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            String line;
            while (running && (line = in.readLine()) != null) {
                handle(line);
            }
        } catch (Exception ignored) {
        } finally {
            close();
        }
    }

    private void handle(String line) throws IOException {
        ApiEnvelope<Serializable> request;
        try {
            request = JacksonSupport.envelopeFromJson(line);
        } catch (Exception e) {
            send(ApiEnvelope.fail(RequestAction.LOGOUT, "Request JSON không hợp lệ: " + e.getMessage()));
            return;
        }
        send(dispatcher.dispatch(request, session, this));
    }

    public synchronized void send(ApiEnvelope<? extends Serializable> envelope) throws IOException {
        if (out == null) return;
        out.write(JacksonSupport.toJson(envelope));
        out.newLine();
        out.flush();
    }

    @Override
    public void onAuctionEvent(AuctionEventDto event) {
        try {
            send(ApiEnvelope.event(RequestAction.SUBSCRIBE_AUCTION, event.message(), event));
        } catch (IOException e) {
            close();
        }
    }

    @Override
    public void close() {
        running = false;
        publisher.unsubscribeAll(this);
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {
        }
    }
}
