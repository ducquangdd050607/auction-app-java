package com.auctionapp.auctionappjava.server.network;

import com.auctionapp.auctionappjava.common.dto.LoginResponse;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
  private final Socket socket;
  // Khởi tạo một Dispatcher duy nhất để điều phối mọi Request
  private final RequestDispatcher dispatcher = new RequestDispatcher();

  public ClientHandler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    String threadName = Thread.currentThread().getName();
    System.out.println("[" + threadName + "] Bat dau phuc vu Client: " + socket.getInetAddress());
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    String currentUserId = null;

    try {
      out = new ObjectOutputStream(socket.getOutputStream());
      out.flush();
      in = new ObjectInputStream(socket.getInputStream());

      while (true) {
        Request request = (Request) in.readObject();

        // Gọi sang Dispatcher để điều hướng
        Response response = dispatcher.dispatch(request);

        // ...nhưng phải xử lí riêng cho session
        if (response.success()) {
          if ("LOGIN".equals(request.action()) && response.data() != null) {
            LoginResponse loginRes = (LoginResponse) response.data();
            currentUserId = loginRes.id();
            SessionManager.getInstance().registerSession(currentUserId, out);
          } else if ("LOGOUT".equals(request.action())) {
            String logoutUserId = (String) request.payload();
            SessionManager.getInstance().removeSession(logoutUserId);
          }
        }

        if (response != null) {
          out.writeObject(response);
          out.flush();
        }
      }

    } catch (Exception e) {
      System.out.println("[" + threadName + "] Client da ngat ket noi. Loi: " + e.getMessage());
    } finally {
      if (currentUserId != null) {
        SessionManager.getInstance().removeSession(currentUserId);
      }

      try {
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
