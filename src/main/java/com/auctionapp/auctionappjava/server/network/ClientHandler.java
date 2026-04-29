package com.auctionapp.auctionappjava.server.network;
import com.auctionapp.auctionappjava.common.dto.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
                Request request = (Request) in.readObject();
                // TODO: nào làm service thì chuyển qua switch-case ở đây, đồng thời đẩy logic qua service
                if("LOGIN".equals(request.action())) {
                    LoginRequest loginData = (LoginRequest) request.payload();
                    String user = loginData.username();
                    String pass = loginData.password();

                    Response response;

                    // TODO: gọi DAO ở đây để xử lí thông tin, ở đây tôi fake database bằng hardcode
                    if (user.equals("admin") && pass.equals("123")) {
                        // Giả lập Database trả về 1 ông Admin
                        LoginResponse admin1 = new LoginResponse("001", "admin", "admin", "ADMIN", null);
                        response = new Response(true, "Đăng nhập thành công Admin!", admin1);

                    } else if (user.equals("quang") && pass.equals("123")) {
                        // Giả lập Database trả về 1 ông Bidder/Seller bình thường
                        LoginResponse user1 = new LoginResponse("002", "quang", "user", "USER", BigDecimal.valueOf(36000000));
                        UserDetailResponse user1Detail = new UserDetailResponse("quang", "Quang", "USER", new BigDecimal(3600000), "ACTIVE", 36);
                        response = new Response(true, "Đăng nhập thành công!", user1);

                    } else if (user.equals("gay") && pass.equals("123")) {
                        LoginResponse user2 = new LoginResponse("002", "gay", "gaylo", "USER", BigDecimal.valueOf(36000000));
                        UserDetailResponse user2Detail = new UserDetailResponse("gay", "gaylo", "USER", new BigDecimal(3600000), "ACTIVE", 12);
                        response = new Response(true, "Đăng nhập thành công!", user2);

                    } else {
                        // Giả lập Database báo không tìm thấy tài khoản
                        response = new Response(false, "Sai thông tin đăng nhập hoặc tài khoản không tồn tại!", null);
                    }

                    // Đóng gói kết quả ném trả lại cho Client
                    out.writeObject(response);
                    // Hàm flush() để đẩy dữ liệu đi đến đích (client) ngay lập tức
                    out.flush();
                } else if ("GET_ALL_AUCTIONS".equals(request.action())) {
                    // TODO: gọi DAO ở đây để xử lí thông tin, ở đây tôi fake database bằng hardcode
                    // Fake danh sách sử dụng BigDecimal
                    List<AuctionSummaryResponse> fakeList = new ArrayList<>();

                    fakeList.add(new AuctionSummaryResponse(
                            "A001", "Điện tử", "iPhone 15 Pro Max",
                            new BigDecimal("25000000"), new BigDecimal("20000000"), new BigDecimal("500000"),
                            "02:15:30", "RUNNING", 12
                    ));

                    fakeList.add(new AuctionSummaryResponse(
                            "A002", "Nghệ thuật", "Tranh sơn dầu",
                            new BigDecimal("0"), new BigDecimal("5000000"), new BigDecimal("200000"),
                            "12:00:00", "OPEN", 5
                    ));

                    fakeList.add(new AuctionSummaryResponse(
                            "A003", "Phương tiện", "Honda SH 150i",
                            new BigDecimal("85000000"), new BigDecimal("70000000"), new BigDecimal("1000000"),
                            "00:45:10", "RUNNING", 25
                    ));

                    // Gói vào Response và gửi đi
                    Response response = new Response(true, "Lấy danh sách thành công", fakeList);
                    out.writeObject(response);
                    out.flush();
                } // TODO: thêm lệnh nhận về ở đây

                else if ("GET_USERS".equals(request.action())) {
                    List<UserDetailResponse> userDetailList = new ArrayList<>();

                    userDetailList.add(new UserDetailResponse("gay", "gaylo", "USER", new BigDecimal(3600000), "ACTIVE", 12));
                    userDetailList.add(new UserDetailResponse("quang", "Quang", "USER", new BigDecimal(3600000), "ACTIVE", 36));

                    Response response = new Response(true, "Lấy danh sách thành công", userDetailList);
                    out.writeObject(response);
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