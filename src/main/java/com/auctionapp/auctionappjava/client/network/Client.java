package com.auctionapp.auctionappjava.client.network;

import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.exception.NetworkException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;

public class Client {
  private static Client instance;
  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;

  // Hàng đợi chặn để trung chuyển Response giữa luồng ngầm và luồng gọi Request
  private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>();

  // Singleton Pattern: Đảm bảo chỉ có 1 NetworkClient tồn tại
  private Client() {}

  public static synchronized Client getInstance() {
    if (instance == null) {
      instance = new Client();
    }
    return instance;
  }

  // Hàm gọi đến khi app vừa khởi động
  public void connect(String serverIp, int port) throws Exception {
    socket = new Socket(serverIp, port);

    // Output luôn khởi tạo trước Input
    out = new ObjectOutputStream(socket.getOutputStream());
    out.flush();
    in = new ObjectInputStream(socket.getInputStream());
    System.out.println("Da ket noi thanh cong toi Server!");

    // Kích hoạt luồng ngầm lắng nghe bằng runAsync
    CompletableFuture.runAsync(() -> listenToServer());
  }

  private void listenToServer() {
    try {
      while (socket != null && !socket.isClosed()) {
        // Treo máy đợi dữ liệu từ Server ném xuống
        Response response = (Response) in.readObject();

        // KIỂM TRA: Nếu là sự kiện Chủ động đẩy từ Server (Push Event)
        if (response.message() != null && response.message().startsWith("SERVER_PUSH_")) {
          // Đẩy sang luồng giao diện JavaFX để xử lý an toàn
          Platform.runLater(() -> RealtimeHandler.handlePushEvent(response));
        }
        // Nếu là Response trả lời thông thường cho một Request vừa gửi
        else {
          responseQueue.put(response); // Nhét vào hàng đợi cho hàm sendRequest lấy ra
        }
      }
    } catch (Exception e) {
      System.out.println("Luong lang nghe da ngat ket noi: " + e.getMessage());
    }
  }

  // Hàm dùng chung cho mọi Controller để gửi Request và lấy Response
  public synchronized Response sendRequest(Request request) throws Exception {
    if (socket == null || socket.isClosed()) {
      throw new NetworkException("Chua ket noi den may chu!");
    }

    // Ném gói tin lên Server
    out.writeObject(request);
    out.flush();

    // Đứng đợi và hứng kết quả Server trả về
    return responseQueue.take();
  }

  // Gọi khi người dùng tắt App
  public void disconnect() {
    try {
      if (in != null) in.close();
      if (out != null) out.close();
      if (socket != null) socket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
