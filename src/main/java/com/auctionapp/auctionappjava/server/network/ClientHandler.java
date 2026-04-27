package com.auctionapp.auctionappjava.server.network;
import com.auctionapp.auctionappjava.common.dto.LoginRequest;
import com.auctionapp.auctionappjava.common.dto.LoginResponse;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Bắt đầu phục vụ Client: " + socket.getInetAddress());
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // TODO Bước 2: Tạo vòng lặp while(true) ở đây để đọc Request
            while (true) {
                Request req = (Request) in.readObject();
                // TODO: nào làm service thì chuyển qua switch-case ở đây, đồng thời đẩy logic qua service
                if("LOGIN".equals(req.action())) {
                    LoginRequest loginData = (LoginRequest) req.payload();
                    String user = loginData.username();
                    String pass = loginData.password();

                    Response response;

                    // TODO: gọi DAO ở đây để xử lí thông tin, ở đây tôi fake database bằng hardcode
                    if (user.equals("admin") && pass.equals("123")) {
                        // Giả lập Database trả về 1 ông Admin
                        LoginResponse admin1 = new LoginResponse("admin", "ADMIN");
                        response = new Response(true, "Đăng nhập thành công Admin!", admin1);

                    } else if (user.equals("quang") && pass.equals("123")) {
                        // Giả lập Database trả về 1 ông Bidder/Seller bình thường
                        LoginResponse user1 = new LoginResponse("quang", "USER");
                        response = new Response(true, "Đăng nhập thành công!", user1);

                    } else {
                        // Giả lập Database báo không tìm thấy tài khoản
                        response = new Response(false, "Sai thông tin đăng nhập hoặc tài khoản không tồn tại!", null);
                    }

                    // Đóng gói kết quả ném trả lại cho Client
                    out.writeObject(response);
                    // Hàm flush() để đẩy dữ liệu đi đến đích (client) ngay lập tức
                    out.flush();
                }
            }

        } catch (Exception e) {
            System.out.println("[" + threadName + "] Client đã ngắt kết nối. Lỗi: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("[" + threadName + "] Trở về trạng thái rảnh rỗi chờ việc mới.");
        }
    }
}