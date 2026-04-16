package com.auctionhub.server.network;

import com.auctionhub.server.config.ServerProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionServer {
    private final ServerProperties properties;
    private final RequestDispatcher dispatcher;
    private final SocketAuctionEventPublisher publisher;
    private final ExecutorService connectionPool = Executors.newCachedThreadPool();
    private volatile boolean running;
    private ServerSocket serverSocket;

    public AuctionServer(ServerProperties properties, RequestDispatcher dispatcher, SocketAuctionEventPublisher publisher) {
        this.properties = properties;
        this.dispatcher = dispatcher;
        this.publisher = publisher;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(properties.getHost(), properties.getPort()), properties.getBacklog());
            serverSocket.setSoTimeout(properties.getAcceptTimeoutMs());
            running = true;
            System.out.printf("Auction server is listening on %s:%d%n", properties.getHost(), properties.getPort());
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    connectionPool.submit(new ClientConnection(socket, dispatcher, publisher));
                } catch (SocketTimeoutException ignored) {
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể khởi động socket server", ex);
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        connectionPool.shutdownNow();
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
