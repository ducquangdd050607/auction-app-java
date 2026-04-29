package com.auctionapp.auctionappjava.server.network;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.factory.UserFactory;
import com.auctionapp.auctionappjava.common.model.User;
import com.auctionapp.auctionappjava.common.model.Wallet;
import com.auctionapp.auctionappjava.common.util.PasswordUtils;
import com.auctionapp.auctionappjava.server.dao.*;
import com.auctionapp.auctionappjava.server.dao.jdbc.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private Socket socket;

    // Khai báo đối tượng DAO (có thể để làm thuộc tính của class)
    private final UserDao userDao = new JdbcUserDao();

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
                    String pass = loginData.password(); // Pass người dùng nhập

                    Response response;

                    try {
                        // 1. Dùng DAO chui xuống MySQL tìm người dùng theo username
                        Optional<User> userOptional = userDao.findByName(user);

                        // 2. Nếu tìm thấy tài khoản trong Database
                        if (userOptional.isPresent()) {
                            User dbUser = userOptional.get();

                            // 3. So sánh mật khẩu (Tạm thời so sánh chuỗi chay)
                            // Nếu hệ thống của bạn có băm mật khẩu, bạn cần hash biến 'pass' trước khi so sánh với dbUser.getPasswordHash()
                            if (PasswordUtils.verifyPassword(pass, dbUser.getPasswordSalt(), dbUser.getPasswordHash())) {

                                // 4. Mật khẩu đúng! Kéo tiếp số dư ví của ông này lên
                                Optional<Wallet> walletOpt = userDao.findWalletByUserId(dbUser.getId());
                                // Nếu có ví thì lấy số dư, chưa có ví thì gán mặc định là 0 đồng
                                BigDecimal balance = walletOpt.isPresent() ? walletOpt.get().getBalance() : BigDecimal.ZERO;

                                // 5. Đóng gói Model từ Database thành DTO gửi về Client
                                LoginResponse loginRes = new LoginResponse(
                                        dbUser.getId().toString(),   // Chuyển UUID thành String cho DTO
                                        dbUser.getUsername(),
                                        dbUser.getFullName(),
                                        dbUser.getRole().name(),     // "ADMIN", "SELLER" hoặc "BIDDER"
                                        balance
                                );

                                response = new Response(true, "Đăng nhập thành công!", loginRes);

                            } else {
                                response = new Response(false, "Sai mật khẩu!", null);
                            }
                        } else {
                            // Không tìm thấy username trong DB
                            response = new Response(false, "Tài khoản không tồn tại!", null);
                        }
                    } catch (Exception e) {
                        // Bắt lỗi Database sập hoặc đứt kết nối
                        e.printStackTrace();
                        response = new Response(false, "Lỗi máy chủ cơ sở dữ liệu!", null);
                    }

                    // Đóng gói kết quả ném trả lại cho Client
                    out.writeObject(response);
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
                // ... (code xử lý LOGIN) ...

                else if ("REGISTER".equals(request.action())) {
                    RegisterRequest regData = (RegisterRequest) request.payload();
                    Response response;

                    try {
                        // 1. Kiểm tra xem username đã tồn tại trong DB chưa?
                        if (userDao.findByName(regData.username()).isPresent()) {
                            response = new Response(false, "Tên đăng nhập đã tồn tại! Vui lòng chọn tên khác.", null);
                        } else {
                            // 2. Tạo đối tượng User mới.
                            // Vì User là Abstract Class, ta dùng UserFactory (giống cách bạn làm trong JdbcUserDao)
                            Role roleEnum = Role.valueOf(regData.role().toUpperCase());
                            User newUser = UserFactory.create(roleEnum);

                            // 3. Gắn dữ liệu (Fake hash password để test trước)
                            newUser.setId(UUID.randomUUID());
                            newUser.setUsername(regData.username());
                            newUser.setPasswordSalt(PasswordUtils.generateSalt());
                            newUser.setPasswordHash(PasswordUtils.hashPassword(regData.password(), newUser.getPasswordSalt()));
                            newUser.setFullName(regData.fullName());
                            newUser.setEmail(regData.email());
                            newUser.setRole(roleEnum);
                            newUser.setActive(true);
                            newUser.setCreatedAt(LocalDateTime.now());
                            newUser.setUpdatedAt(LocalDateTime.now());

                            // 4. Gọi DAO để INSERT xuống TiDB
                            userDao.save(newUser);

                            // 5. Tạo luôn một cái Ví (Wallet) 0 đồng cho tài khoản mới này
                            Wallet newWallet = new Wallet(
                                    UUID.randomUUID(),
                                    LocalDateTime.now(),
                                    LocalDateTime.now(),
                                    newUser.getId(),
                                    java.math.BigDecimal.ZERO
                            );
                            userDao.saveWallet(newWallet);

                            // 6. Báo thành công
                            response = new Response(true, "Đăng ký tài khoản thành công!", null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        response = new Response(false, "Lỗi máy chủ khi lưu dữ liệu!", null);
                    }

                    // Gửi kết quả về cho Client
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