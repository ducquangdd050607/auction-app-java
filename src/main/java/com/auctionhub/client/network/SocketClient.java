package com.auctionhub.client.network;

import com.auctionhub.common.dto.ApiEnvelope;
import com.auctionhub.common.enums.EventType;
import com.auctionhub.common.enums.MessageKind;
import com.auctionhub.common.enums.RequestAction;
import com.auctionhub.common.util.JacksonSupport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SocketClient {
    private final String host;
    private final int port;
    private final Map<String, CompletableFuture<ApiEnvelope>> pendingRequests = new ConcurrentHashMap<>();
    private final Map<EventType, List<Consumer<ApiEnvelope>>> eventListeners = new ConcurrentHashMap<>();
    private final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private volatile boolean connected;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void connectIfNeeded() {
        if (connected) {
            return;
        }
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            connected = true;
            listenerExecutor.submit(this::listenLoop);
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể kết nối tới server " + host + ":" + port, ex);
        }
    }

    public ApiEnvelope send(RequestAction action, Object payload) {
        connectIfNeeded();
        String requestId = UUID.randomUUID().toString();
        ApiEnvelope request = ApiEnvelope.request(action, requestId, payload == null ? null : JacksonSupport.toNode(payload));
        CompletableFuture<ApiEnvelope> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        try {
            synchronized (this) {
                writer.write(JacksonSupport.toJson(request));
                writer.newLine();
                writer.flush();
            }
            return future.orTimeout(10, TimeUnit.SECONDS).join();
        } catch (Exception ex) {
            pendingRequests.remove(requestId);
            throw new CompletionException(new IllegalStateException("Không nhận được phản hồi từ server.", ex));
        }
    }

    public void onEvent(EventType eventType, Consumer<ApiEnvelope> handler) {
        eventListeners.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public void clearEventListeners() {
        eventListeners.clear();
    }

    public synchronized void disconnect() {
        connected = false;
        pendingRequests.values().forEach(future -> future.completeExceptionally(new IllegalStateException("Kết nối đã đóng.")));
        pendingRequests.clear();
        clearEventListeners();
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void listenLoop() {
        try {
            String line;
            while (connected && (line = reader.readLine()) != null) {
                ApiEnvelope envelope = JacksonSupport.fromJson(line, ApiEnvelope.class);
                if (envelope.getKind() == MessageKind.RESPONSE) {
                    CompletableFuture<ApiEnvelope> future = pendingRequests.remove(envelope.getRequestId());
                    if (future != null) {
                        future.complete(envelope);
                    }
                } else if (envelope.getKind() == MessageKind.EVENT) {
                    eventListeners.getOrDefault(envelope.getEventType(), List.of()).forEach(handler -> handler.accept(envelope));
                }
            }
        } catch (IOException ex) {
            if (connected) {
                pendingRequests.values().forEach(future -> future.completeExceptionally(ex));
            }
        } finally {
            disconnect();
        }
    }
}
