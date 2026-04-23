package com.auctionapp.auctionappjava.server.network;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class AuctionServer implements Closeable {
    private final int port;
    private final RequestDispatcher dispatcher;
    private final SocketAuctionEventPublisher publisher;
    private final ExecutorService clients = Executors.newCachedThreadPool();
    private volatile boolean running;
    private ServerSocket serverSocket;

    public AuctionServer(int port, RequestDispatcher dispatcher, SocketAuctionEventPublisher publisher) {
        this.port = port;
        this.dispatcher = dispatcher;
        this.publisher = publisher;
    }

    public void start() {
        running = true;
        try (ServerSocket ss = new ServerSocket(port)) {
            serverSocket = ss;
            System.out.println("AuctionServer listening on port " + port);
            while (running) {
                Socket s = ss.accept();
                clients.submit(new ClientConnection(s, dispatcher, publisher));
            }
        } catch (IOException e) {
            if (running) throw new IllegalStateException("Server socket lỗi", e);
        }
    }

    @Override
    public void close() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
        clients.shutdownNow();
    }
}
