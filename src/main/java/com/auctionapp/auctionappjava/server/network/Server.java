package com.auctionapp.auctionappjava.server.network;

import com.auctionapp.auctionappjava.server.dao.db.DatabaseManager;
import com.auctionapp.auctionappjava.server.service.AuctionStatusService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
  public static void startServer(int port, int maxClients) {
    System.out.println("Khoi dong server");
    AuctionStatusService.recoverAndScheduleAll();
    ExecutorService threadPool = Executors.newFixedThreadPool(maxClients);

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server dang lang nghe o cong " + port + "...");

      // While ở đây để khi nào client ngắt kết nối mới dừng, không thì server luôn mở để nhận
      // request từ client
      while (true) {
        // Đứng đợi Client kết nối
        Socket clientSocket = serverSocket.accept();
        System.out.println("Co client moi ket noi: " + clientSocket.getInetAddress());

        // TODO: Đẩy request sang ClientHandler và add socket vào thread pool
        ClientHandler handler = new ClientHandler(clientSocket);
        threadPool.execute(handler);
      }
    } catch (IOException e) {
      System.err.println("Loi khoi dong server: " + e.getMessage());
    } finally {
      if (!threadPool.isShutdown()) {
        threadPool.shutdown();
      }
      AuctionStatusService.shutdown();
      DatabaseManager.getInstance().shutdown();
    }
  }
}
