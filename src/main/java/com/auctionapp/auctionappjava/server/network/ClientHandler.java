package com.auctionapp.auctionappjava.server.network;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.factory.AuctionItemFactory;
import com.auctionapp.auctionappjava.common.factory.UserFactory;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.common.model.Item;
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

    // Khai báo các DAO cần thiết ở đầu ClientHandler
    private final UserDao userDao = new JdbcUserDao();
    private final AuctionDao auctionDao = new JdbcAuctionDao();
    private final AuctionItemDao itemDao = new JdbcAuctionItemDao();
    private final BidDao bidDao = new JdbcBidDao();

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
                    String username = loginData.username();
                    String pass = loginData.password(); // Pass người dùng nhập

                    Response response;

                    try {
                        // 1. Dùng DAO chui xuống MySQL tìm người dùng theo username
                        Optional<User> userOptional = userDao.findByName(username);

                        // 2. Nếu tìm thấy tài khoản trong Database
                        if (userOptional.isPresent()) {
                            User user = userOptional.get();

                            // 3. So sánh mật khẩu
                            if (PasswordUtils.verifyPassword(pass, user.getPasswordSalt(), user.getPasswordHash())) {

                                // 4. Nếu pass đúng, kéo tiếp số dư ví của ông này lên
                                BigDecimal balance = userDao.findWalletByUserId(user.getId()).get().getBalance();

                                // 5. Đóng gói Model từ Database thành DTO gửi về Client
                                LoginResponse loginRes = new LoginResponse(
                                        user.getId().toString(),   // Chuyển UUID thành String cho DTO
                                        user.getUsername(),
                                        user.getFullName(),
                                        user.getRole().name(),     // "ADMIN", "SELLER" hoặc "BIDDER"
                                        user.getEmail(),
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
                    try {
                        // 1. Kéo toàn bộ danh sách Phiên đấu giá từ Database lên
                        List<Auction> dbAuctions = auctionDao.findAll();

                        // 2. Chuẩn bị một chiếc hộp rỗng để chứa các DTO gửi về Client
                        List<AuctionSummaryResponse> responseList = new ArrayList<>();

                        // 3. Dây chuyền lắp ráp: Lắp thông tin Item và Bid vào từng Auction
                        for (Auction auction : dbAuctions) {

                            // Chui vào kho (bảng auction_items) để lấy tên và loại đồ vật dựa vào itemId
                            Optional<Item> itemOptional = itemDao.findById(auction.getItemId());

                            if (itemOptional.isPresent()) {
                                Item item = itemOptional.get();

                                // Chui vào bảng bids để đếm xem có bao nhiêu lượt đặt giá cho phiên này
                                int bidderCount = (int) bidDao.countByAuctionId(auction.getId());

                                // TODO: Xử lý thời gian còn lại (timeLeft)
                                // Tạm thời để một chuỗi text, sau này bạn có thể viết hàm trừ EndTime cho Time.now()
                                String timeLeftStr = "Đang diễn ra";

                                // Lắp ráp toàn bộ dữ liệu thành 1 cái DTO chuẩn chỉ
                                AuctionSummaryResponse dto = new AuctionSummaryResponse(
                                        auction.getId().toString(),
                                        item.getItemType().name(),        // Loại lấy từ bảng Item
                                        item.getTitle(),                  // Tên lấy từ bảng Item
                                        item.getStartingPrice(),          // Giá khởi điểm từ Item
                                        auction.getCurrentPrice(),        // Giá hiện tại từ bảng Auction
                                        auction.getMinimumIncrement(),    // Bước giá
                                        auction.getStatus().name(),       // Trạng thái từ bảng Auction
                                        timeLeftStr,
                                        bidderCount                       // Số lượt bid từ bảng Bids
                                );

                                // Bỏ DTO vào hộp
                                responseList.add(dto);
                            }
                        }

                        // 4. Gói ghém cẩn thận và ném qua Socket về cho màn hình JavaFX
                        Response response = new Response(true, "Tải dữ liệu thành công!", responseList);
                        out.writeObject(response);
                        out.flush();

                    } catch (Exception e) {
                        e.printStackTrace();
                        // Báo lỗi nếu đứt mạng hoặc sập TiDB
                        out.writeObject(new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null));
                        out.flush();
                    }
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
                    RegisterRequest registerRequest = (RegisterRequest) request.payload();
                    Response response;

                    try {
                        // 1. Kiểm tra xem username đã tồn tại trong DB chưa?
                        if (userDao.findByName(registerRequest.username()).isPresent()) {
                            response = new Response(false, "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!", null);
                        } else {
                            // 2. Tạo đối tượng User mới (Abstract factory ở UserFactory)
                            Role roleEnum = Role.valueOf(registerRequest.role().toUpperCase());
                            User newUser = UserFactory.create(roleEnum);

                            // 3. Gắn dữ liệu
                            newUser.setId(UUID.randomUUID());
                            newUser.setUsername(registerRequest.username());
                            newUser.setPasswordSalt(PasswordUtils.generateSalt());
                            newUser.setPasswordHash(PasswordUtils.hashPassword(registerRequest.password(), newUser.getPasswordSalt()));
                            newUser.setFullName(registerRequest.fullName());
                            newUser.setEmail(registerRequest.email());
                            newUser.setRole(roleEnum);
                            newUser.setActive(true);
                            newUser.setCreatedAt(LocalDateTime.now());
                            newUser.setUpdatedAt(LocalDateTime.now());

                            // 4. Gọi DAO để INSERT xuống database
                            userDao.save(newUser);

                            // 5. Tạo luôn một cái Ví (Wallet) 0 đồng cho tài khoản mới này
                            Wallet newWallet = new Wallet(
                                    UUID.randomUUID(),
                                    LocalDateTime.now(),
                                    LocalDateTime.now(),
                                    newUser.getId(),
                                    BigDecimal.ZERO
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
                } else if ("ADD_ITEM".equals(request.action())) {
                    AddItemRequest data = (AddItemRequest) request.payload();
                    Response response;

                    try {
                        // 1. Tạo ID ngẫu nhiên cho vật phẩm mới
                        UUID newItemId = UUID.randomUUID();
                        UUID sellerId = UUID.fromString(data.sellerId());

                        // 2. KHỞI TẠO VÀ LƯU VẬT PHẨM (Sử dụng AuctionItemFactory giống trong JdbcAuctionItemDao)
                        Item newItem = AuctionItemFactory.create(
                                ItemType.valueOf(data.itemType().toUpperCase()), // Ép kiểu chuỗi về Enum ItemType
                                newItemId,
                                data.openTime(),
                                data.openTime(),
                                sellerId,
                                data.title(),
                                data.description(),
                                data.startPrice(),
                                data.attribute1(),
                                data.attribute2()
                        );

                        itemDao.save(newItem); // Gọi lệnh INSERT xuống bảng auction_items

                        // 3. KHỞI TẠO VÀ LƯU PHIÊN ĐẤU GIÁ LIÊN KẾT VỚI VẬT PHẨM ĐÓ
                        Auction newAuction = new Auction(
                                UUID.randomUUID(), // ID phiên đấu giá
                                data.openTime(), // Thời gian tạo
                                data.openTime(), // Thời gian cập nhật
                                newItemId,           // Liên kết khóa ngoại với ID vật phẩm vừa tạo
                                sellerId,          // ID người bán
                                data.startPrice(),   // Giá hiện tại lúc bắt đầu chính là giá khởi điểm
                                null,                // Chưa có ai đấu giá (Leading Bidder = null)
                                data.openTime(), // Bắt đầu đấu giá ngay lập tức
                                data.endTime(), // Kết thúc sau X ngày
                                AuctionStatus.OPEN,  // Trạng thái phiên
                                data.minIncrement(), // Bước giá tối thiểu
                                null                 // Chưa có người chiến thắng
                        );

                        auctionDao.save(newAuction); // Gọi lệnh INSERT xuống bảng auctions

                        // 4. Báo cáo thành công
                        response = new Response(true, "Đăng bán sản phẩm thành công! Phiên đấu giá đã được mở.", null);

                    } catch (IllegalArgumentException e) {
                        response = new Response(false, "Sai định dạng loại sản phẩm!", null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        response = new Response(false, "Lỗi máy chủ khi lưu sản phẩm: " + e.getMessage(), null);
                    }

                    out.writeObject(response);
                    out.flush();
                } else if("DEPOSIT".equals(request.action())) {
                    DepositRequest depositRequest = (DepositRequest) request.payload();
                    String userId = depositRequest.userId();
                    BigDecimal amount = depositRequest.amount();
                    Response response;

                    try {

                        // 2. Tìm ví của User (Sử dụng Optional để tránh NullPointerException)
                        Wallet wallet = userDao.findWalletByUserId(UUID.fromString(userId))
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví cho người dùng này"));

                        // 3. Cộng tiền (BigDecimal là immutable nên phải gán lại kết quả)
                        BigDecimal newBalance = wallet.getBalance().add(amount);
                        wallet.setBalance(newBalance);
                        userDao.saveWallet(wallet);

                        response = new Response(true, "Nạp thành gay", null);

                    } catch (Exception e) {
                        // Log lỗi và xử lý
                        e.printStackTrace();
                        response = new Response(false, "Nạp thất bại", null);
                    }

                    out.writeObject(response);
                    out.flush();

                } else if ("CHANGE_INFORMATION".equals(request.action())) {
                    ChangeInformationRequest data = (ChangeInformationRequest) request.payload();
                    String userId = data.userId();
                    String newFullName = data.fullName();
                    String newEmail = data.email();
                    Response response;

                    try {
                        // 1. Tìm User đó
                        User user = userDao.findById(UUID.fromString(userId))
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng này"));
                        // 2. Thay đổi dữ liệu

                        user.setFullName(newFullName);
                        user.setEmail(newEmail);
                        userDao.save(user);

                        response = new Response(true, "Thay đổi thành công", null);

                    } catch (Exception e) {
                        e.printStackTrace();
                        response = new Response(false, "Lỗi máy chủ khi thay đổi thông tin", null);
                    }

                    out.writeObject(response);
                    out.flush();

                } else if ("CHANGE_PASSWORD".equals(request.action())) {
                    ChangePasswordRequest data =  (ChangePasswordRequest) request.payload();
                    String userId = data.userId();
                    String newPassword = data.newPassword();
                    Response response;

                    try {
                        User user = userDao.findById(UUID.fromString(userId))
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng này"));

                        user.setPasswordSalt(PasswordUtils.generateSalt());
                        user.setPasswordHash(PasswordUtils.hashPassword(newPassword, user.getPasswordSalt()));

                        userDao.save(user);

                        response = new Response(true, "Đổi mật khẩu thành công", null);

                    } catch (Exception e) {
                        e.printStackTrace();
                        response = new Response(false, "Lỗi máy chủ khi thay đổi mật khẩu", null);
                    }
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