package com.auctionapp.auctionappjava.server.network;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.ServerSocket;

public class Server {
    private static final int PORT = 8080;
    private static final int MAX_CLIENTS = 50;

    public static void startServer() {
        System.out.println("Khởi động Server");
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đang lắng nghe ở cổng " + PORT + "...");

            // While ở đây để khi nào client ngắt kết nối mới dừng, không thì server luôn mở để nhận request từ client
            while (true) {
                // Đứng đợi Client kết nối
                Socket clientSocket = serverSocket.accept();
                System.out.println("Có Client mới kết nối: " + clientSocket.getInetAddress());

                // TODO: Đẩy request sang ClientHandler và add socket vào thread pool
                /*ClientHandler handler = new ClientHandler(clientSocket);
                threadPool.execute(handler);*/
            }
        } catch (IOException e) {
            System.err.println("Lỗi khởi động Server: " + e.getMessage());
        } finally {
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdown();
            }
        }
    }
}