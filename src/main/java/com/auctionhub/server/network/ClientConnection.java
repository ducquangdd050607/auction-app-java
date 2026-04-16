package com.auctionhub.server.network;

import com.auctionhub.common.dto.ApiEnvelope;
import com.auctionhub.common.observer.AuctionEventListener;
import com.auctionhub.common.util.JacksonSupport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientConnection implements Runnable, AuctionEventListener {
    private final Socket socket;
    private final RequestDispatcher dispatcher;
    private final SocketAuctionEventPublisher publisher;
    private final ClientSession session = new ClientSession();
    private BufferedWriter writer;
    private volatile boolean running = true;

    public ClientConnection(Socket socket, RequestDispatcher dispatcher, SocketAuctionEventPublisher publisher) {
        this.socket = socket;
        this.dispatcher = dispatcher;
        this.publisher = publisher;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            this.writer = bufferedWriter;
            String line;
            while (running && (line = reader.readLine()) != null) {
                ApiEnvelope request = JacksonSupport.fromJson(line, ApiEnvelope.class);
                ApiEnvelope response = dispatcher.dispatch(request, session, this);
                send(response);
            }
        } catch (IOException ex) {
            if (running) {
                System.err.println("Client connection error: " + ex.getMessage());
            }
        } finally {
            shutdown();
        }
    }

    @Override
    public void onAuctionEvent(ApiEnvelope event) {
        send(event);
    }

    public synchronized void send(ApiEnvelope envelope) {
        if (writer == null) {
            return;
        }
        try {
            writer.write(JacksonSupport.toJson(envelope));
            writer.newLine();
            writer.flush();
        } catch (IOException ex) {
            shutdown();
        }
    }

    public void shutdown() {
        running = false;
        publisher.unsubscribeAll(this);
        session.logout();
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
