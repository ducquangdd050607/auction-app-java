package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.factory.UserFactory;
import com.auctionapp.auctionappjava.common.model.User;
import com.auctionapp.auctionappjava.common.model.Wallet;
import com.auctionapp.auctionappjava.common.util.PasswordUtils;
import com.auctionapp.auctionappjava.server.dao.AuctionDao;
import com.auctionapp.auctionappjava.server.dao.BidDao;
import com.auctionapp.auctionappjava.server.dao.UserDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcAuctionDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcBidDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcUserDao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDateTime.now;

public class UserService {
    // Service sẽ ôm các DAO tương ứng
    private final UserDao userDao = new JdbcUserDao();
    private final BidDao bidDao = new JdbcBidDao();
    private final AuctionDao auctionDao = new JdbcAuctionDao();

    public Response handleLogin(LoginRequest loginData) {
        try {
            Optional<User> userOptional = userDao.findByName(loginData.username());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (PasswordUtils.verifyPassword(loginData.password(), user.getPasswordSalt(), user.getPasswordHash())) {
                    BigDecimal balance = userDao.findWalletByUserId(user.getId()).get().getBalance();
                    LoginResponse loginRes = new LoginResponse(
                            user.getId().toString(), user.getUsername(), user.getFullName(),
                            user.getRole().name(), user.getEmail(), balance, user.isActive()
                    );

                    if (!user.isActive()) {
                        return new Response(false, "Tài khoản đã bị chặn", null);
                    }

                    return new Response(true, "Đăng nhập thành công!", loginRes);
                } else {
                    return new Response(false, "Sai mật khẩu!", null);
                }
            }
            return new Response(false, "Tài khoản không tồn tại!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ cơ sở dữ liệu!", null);
        }
    }

    public Response handleDeposit(DepositRequest depositData) {
        try {
            Optional<Wallet> wallet = userDao.findWalletByUserId(UUID.fromString(depositData.userId()));
            if (wallet.isPresent()) {
                Wallet depositWallet = wallet.get();
                depositWallet.setBalance(depositWallet.getBalance().add(depositData.amount()));
                userDao.saveWallet(depositWallet);
                return new Response(true, "Nạp tiền thành công", null);
            } else return new Response(false, "Có lỗi khi nạp tiền", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Nạp thất bại", null);
        }
    }

    public Response handleRegister(RegisterRequest registerData) {
        try {
            // 1. Kiểm tra xem username đã tồn tại trong DB chưa?
            if (userDao.findByName(registerData.username()).isPresent()) {
                return new Response(false, "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!", null);
            } else {
                // 2. Tạo đối tượng User mới (Abstract factory ở UserFactory)
                Role roleEnum = Role.valueOf(registerData.role().toUpperCase());
                User newUser = UserFactory.create(roleEnum);

                // 3. Gắn dữ liệu
                newUser.setId(UUID.randomUUID());
                newUser.setUsername(registerData.username());
                newUser.setPasswordSalt(PasswordUtils.generateSalt());
                newUser.setPasswordHash(PasswordUtils.hashPassword(registerData.password(), newUser.getPasswordSalt()));
                newUser.setFullName(registerData.fullName());
                newUser.setEmail(registerData.email());
                newUser.setRole(roleEnum);
                newUser.setActive(true);
                newUser.setCreatedAt(now());
                newUser.setUpdatedAt(now());

                // 4. Gọi DAO để INSERT xuống database
                userDao.save(newUser);

                // 5. Tạo luôn một cái Ví (Wallet) 0 đồng cho tài khoản mới này
                Wallet newWallet = new Wallet(
                        UUID.randomUUID(),
                        now(),
                        now(),
                        newUser.getId(),
                        BigDecimal.ZERO
                );
                userDao.saveWallet(newWallet);

                // 6. Báo thành công
                return new Response(true, "Đăng ký tài khoản thành công!", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi lưu dữ liệu!", null);
        }
    }

    public Response handleChangeInformation(ChangeInformationRequest changeInformationData) {
        String userId = changeInformationData.userId();
        String newFullName = changeInformationData.fullName();
        String newEmail = changeInformationData.email();

        try {
            // 1. Tìm User đó
            User user = userDao.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng này"));
            // 2. Thay đổi dữ liệu

            user.setFullName(newFullName);
            user.setEmail(newEmail);
            userDao.save(user);

            return new Response(true, "Thay đổi thành công", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi thay đổi thông tin", null);
        }
    }

    public Response handleChangePassword(ChangePasswordRequest changePasswordData) {
        String userId = changePasswordData.userId();
        String newPassword = changePasswordData.newPassword();

        try {
            User user = userDao.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng này"));

            user.setPasswordSalt(PasswordUtils.generateSalt());
            user.setPasswordHash(PasswordUtils.hashPassword(newPassword, user.getPasswordSalt()));

            userDao.save(user);

            return new Response(true, "Đổi mật khẩu thành công", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi thay đổi mật khẩu", null);
        }
    }

    public Response handleGetBalance(String userId) {
        try {
            Optional<Wallet> walletOpt = userDao.findWalletByUserId(UUID.fromString(userId));
            if (walletOpt.isPresent()) {
                // Trả về số dư mới nhất
                return new Response(true, "Lấy số dư thành công", walletOpt.get().getBalance());
            }
            return new Response(false, "Không tìm thấy ví", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi khi lấy số dư", null);
        }
    }

    public Response handleGetStats(String userId) {
        try {
            Optional<User> userOpt = userDao.findById(UUID.fromString(userId));

            long countersByRole = 0;

            long runningAuctions = auctionDao.countRunningAuctions();

            if (userOpt.isPresent()) {

                User user = userOpt.get();
                if (user.getRole() == Role.BIDDER) {

                    countersByRole = bidDao.countBidsByBidderId(user.getId());

                }

                else if (user.getRole() == Role.SELLER) {

                    countersByRole = auctionDao.countAuctionsCreatedBySellerId(user.getId());

                }

                else if (user.getRole() == Role.ADMIN) {

                    countersByRole = userDao.countUsersActive();

                }
                ArrayList<Object> pushData = new ArrayList<>();

                pushData.add(countersByRole);
                pushData.add(runningAuctions);

                return new Response(true, "Xác định các chỉ số xong", pushData);
            }
            return new Response(false, "Người dùng không tồn tại", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ", null);
        }
    }
}